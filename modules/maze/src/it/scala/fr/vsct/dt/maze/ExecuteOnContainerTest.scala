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
package fr.vsct.dt.maze

import com.github.dockerjava.api.command.CreateContainerCmd
import fr.vsct.dt.maze.core.Commands
import fr.vsct.dt.maze.test.macros.DockerClientWithDelayedStart
import fr.vsct.dt.maze.topology.Docker.DockerProcessExecution
import fr.vsct.dt.maze.topology.{Docker, SingleContainerClusterNode}

import scala.util.{Failure, Try}


class ExecuteOnContainerTest extends TechnicalTest {

  class DummyContainer extends SingleContainerClusterNode {
    override def serviceContainer: CreateContainerCmd = return "busybox".withEntrypoint("sleep", "2000s")

    override val servicePort: Int = 9000
  }

  var container: DummyContainer = _

  "execution on container" should "not be checked before it actually started" in {
    Docker.client = DockerClientWithDelayedStart(Docker.client)

    val result: Array[String] = Commands.exec(container.shellExecution("/bin/sh", "-c", "echo done!"))
    result shouldBe Array("done!")
  }

  "execution on container" should "wait until execution is over" in {

    val start = System.currentTimeMillis()
    val result: Array[String] = Commands.exec(container.shellExecution("/bin/sh", "-c", "sleep 10s && echo done!"))
    result shouldBe Array("done!")

    val duration = System.currentTimeMillis() - start
    duration should be >= 10000L
    duration should be <= 11000L
  }

  "execution on container" should "handle return codes != 0 as errors" in {
    val result: Try[Array[String]] = container.shellExecution("/bin/sh", "-c", "sleep sometime").execute()

    result match {
      case Failure(DockerProcessExecution(_, _)) => // it's what we expect
      case other => fail(s"Execution failure returned $other instead of Failure(DockerProcessExecution(_, _))")
    }
  }

  override protected def beforeEach(): Unit = {
    container = new DummyContainer
    container.start()
  }

  override protected def afterEach(): Unit = {
    container.clear()
  }
}
