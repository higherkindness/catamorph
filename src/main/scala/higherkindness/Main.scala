/*
 * Copyright 2018 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package higherkindness

import java.io.File

import cats.effect.IO
import fs2.{Stream, StreamApp}
import higherkindness.db.{DBService, DBServiceStorage}
import higherkindness.protocol.{ProtocolService, ProtocolServiceStorage}
import higherkindness.http.RootService
import higherkindness.models.{CompendiumConfig, HttpConfig}
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object Main extends StreamApp[IO] {

  def server(
      conf: HttpConfig,
      dbService: DBService[IO],
      protocolService: ProtocolService[IO]): Stream[IO, StreamApp.ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(conf.port, conf.host)
      .mountService(RootService.rootRouteService(protocolService, dbService), "/")
      .serve(IO.ioConcurrentEffect, ExecutionContext.global)

  override def stream(
      args: List[String],
      requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] =
    for {
      conf <- Stream.eval(IO(pureconfig.loadConfigOrThrow[CompendiumConfig]))
      _    <- Stream.eval(IO { new File(conf.storage.path).mkdir() })
      code <- server(
        conf.http,
        DBServiceStorage.impl(conf.storage),
        ProtocolServiceStorage.impl(conf.storage))
    } yield code
}