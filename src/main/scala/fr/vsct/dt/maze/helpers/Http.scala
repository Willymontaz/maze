/*
 * Copyright (C) 2016 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.dt.maze.helpers

import java.io.{BufferedReader, InputStreamReader}

import com.typesafe.scalalogging.StrictLogging
import fr.vsct.dt.maze.core.Execution
import fr.vsct.dt.maze.topology.DockerClusterNode
import org.apache.http
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager

import scala.language.implicitConversions
import scala.util.Try


object Http extends StrictLogging {

  val defaultConnectTimeout: Int = 5000
  val defaultSocketTimeout: Int = 5000
  val defaultConnectionRequestTimeout: Int = 200
  val defaultMaxConnections: Int = 100
  val defaultMaxConnectionsPerRoute: Int = 50

  private def initHttpClient(): CloseableHttpClient = {
    val requestConfig: RequestConfig = RequestConfig.custom()
      .setConnectTimeout(defaultConnectTimeout)
      .setSocketTimeout(defaultSocketTimeout)
      .setConnectionRequestTimeout(defaultConnectionRequestTimeout)
      .build()


    val connectionManager = new PoolingHttpClientConnectionManager()
    connectionManager.setDefaultMaxPerRoute(defaultMaxConnectionsPerRoute)
    connectionManager.setMaxTotal(defaultMaxConnections)

    HttpClientBuilder.create()
      .useSystemProperties()
      .setConnectionManager(connectionManager)
      .setDefaultRequestConfig(requestConfig)
      .build()
  }

  // Use var to allow anyone to override it
  var client: CloseableHttpClient = initHttpClient()

  def get(url: String): Execution[HttpResponse] = {
    execute(new HttpGet(url))
  }

  def put(url: String, data: String, contentType: String): Execution[HttpResponse] = {
    val request = new HttpPut(url)
    request.setEntity(new StringEntity(data, ContentType.create(contentType)))
    execute(request)
  }

  def post(url: String, data: String, contentType: String): Execution[HttpResponse] = {
    val request = new HttpPost(url)
    request.setEntity(new StringEntity(data, ContentType.create(contentType)))
    execute(request)
  }

  def execute(request: HttpUriRequest): Execution[HttpResponse] = {
    Execution[HttpResponse] {
      logger.debug(s"calling ${request.toString}")
      val response: CloseableHttpResponse = client.execute(request)
      val result = HttpResponse(response)
      Try(response.close())
      logger.debug(s"Got response ${result.toString}")
      result
    }.labeled(request.toString)
  }

  trait HttpResponse {
    def entity: String
    def headers: Headers
    def responseCode: Int
  }

  case class StringHttpResponse(
                                 override val entity: String,
                                 override val headers: Headers,
                                 override val responseCode: Int) extends HttpResponse

  class Headers(values: Array[Header]) {

    private val headersAsMap: Map[String, Header] = values.map {h => (h.name, h)}.toMap

    def header(name: String): Header = headersAsMap(name)
    def headers: Map[String, Header] = headersAsMap

    override def toString: String = values.map { header =>
      s"${header.name} -> ${header.value}"
    }.mkString("[", ", ", "]")

  }

  case class Header(name: String, value: String)

  object Headers {
    def header(h: http.Header): Header = {
      Header(name = h.getName, value = h.getValue)
    }

    def apply(response: CloseableHttpResponse): Headers = {
      new Headers(response.getAllHeaders.map(header))
    }
  }

  object HttpResponse {

    private def read(in: java.io.InputStream): String = {
      val content = new BufferedReader(new InputStreamReader(in, "UTF-8"))
      val builder = new StringBuilder
      content.lines().forEach { t: String => builder.append(t) }
      builder.toString()
    }

    def apply(response: CloseableHttpResponse): HttpResponse = {
      val stream: Option[String] = Option(response.getEntity).flatMap(e => Option(e.getContent)).map(read)
      StringHttpResponse(
        responseCode = response.getStatusLine.getStatusCode,
        entity = stream.orNull,
        headers = Headers(response)
      )
    }

  }

  trait HttpEnabled { self: DockerClusterNode =>

    def httpGet(path: String): Execution[HttpResponse] = {
      Http.get(s"$baseUrl$path")
    }

    def httpPost(path: String, data: String, contentType: String): Execution[HttpResponse] = {
      Http.post(s"$baseUrl$path", data, contentType)
    }

    def httpPut(path: String, data: String, contentType: String): Execution[HttpResponse] = {
      Http.put(s"$baseUrl$path", data, contentType)
    }

    private def baseUrl = s"http://$externalIp:${mappedPort.getOrElse(
      throw new IllegalStateException("No port found on container, check it is correctly started and that the mapped port is correctly declared.")
    )}"
  }

}
