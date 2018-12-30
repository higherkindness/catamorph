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

import cats.effect.IO
import fs2.{Stream, StreamApp}
import higherkindness.db.DBServiceStorage
import higherkindness.http.RootService
import higherkindness.models.CompendiumConfig
import higherkindness.storage.{FileStorage, StorageService}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends StreamApp[IO] {

  override def stream(
      args: List[String],
      requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] =
    for {
      conf <- Stream.eval(IO(pureconfig.loadConfigOrThrow[CompendiumConfig]))
      storage        = FileStorage.impl[IO](conf.storage)
      storageService = StorageService.impl[IO](storage)
      dbService      = DBServiceStorage.impl[IO](storage)
      service        = RootService.rootRouteService(storageService, dbService)
      code <- CompendiumServerStream.serverStream(conf.http, service)
    } yield code
}
