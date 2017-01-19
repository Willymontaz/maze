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

startYear := Some(2017)
description := """Maze macros enables miscallaneous features for maze"""

licenses := Seq("Apache 2.0" -> new URL("http://www.apache.org/licenses/LICENSE-2.0"))

organizationName := "VSCT"
organizationHomepage := Some(url("http://www.voyages-sncf.com"))

scmInfo := Some(ScmInfo(
  browseUrl = url("http://github.com/voyages-sncf-technologies/maze-macros/tree/master"),
  connection = "scm:git:git://github.com/voyages-sncf-technologies/maze-macros.git",
  devConnection = Some("scm:git:ssh://github.com:voyages-sncf-technologies/maze-macros.git")))

developers := List (
  Developer(
    id = "wmontaz",
    name = "William Montaz",
    email = "wmontaz@voyages-sncf.com",
    url = url("http://www.github.com/willymontaz")
  )
)

homepage := Some(url("http://github.com/voyages-sncf-technologies/maze-macros/tree/master"))
name := s"${organization.value}:${moduleName.value}"
pomIncludeRepository := {repo => false}

publishMavenStyle := true

publishArtifact in Test := false