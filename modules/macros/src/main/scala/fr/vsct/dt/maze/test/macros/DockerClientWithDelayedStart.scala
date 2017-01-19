package fr.vsct.dt.maze.test.macros

import com.github.dockerjava.api.DockerClient

import language.experimental.macros
import reflect.macros.Context

/**
  * ~  Copyright (C) 2016 VSCT
  * ~
  * ~  Licensed under the Apache License, Version 2.0 (the "License");
  * ~  you may not use this file except in compliance with the License.
  * ~  You may obtain a copy of the License at
  * ~
  * ~   http://www.apache.org/licenses/LICENSE-2.0
  * ~
  * ~  Unless required by applicable law or agreed to in writing, software
  * ~  distributed under the License is distributed on an "AS IS" BASIS,
  * ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * ~  See the License for the specific language governing permissions and
  * ~  limitations under the License.
  * ~
  */
object DockerClientWithDelayedStart {

  def apply(instance: DockerClient): DockerClient with DockerClientWithDelayedStart = macro impl

  def impl(c: Context)(instance: c.Expr[com.github.dockerjava.api.DockerClient]) = {
    import c.universe._

    val classDecl = instance.tree
    val (className, fields, parents, body) = classDecl match {
      case q"class $className(..$fields) extends ..$parents { ..$body }" => (className, fields, parents, body)
    }

    val params = fields.asInstanceOf[List[ValDef]] map { p => p.duplicate }

    //Create the new class definition

    q"""
      class $className ( ..$params ) extends ..$parents with DelayedStart {
        ..$body
      }
    """
  }
}

trait DockerClientWithDelayedStart {
  this: DockerClient =>

}