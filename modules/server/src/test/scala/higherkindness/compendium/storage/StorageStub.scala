/*
 * Copyright 2018-2019 47 Degrees, LLC. <http://www.47deg.com>
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

package higherkindness.compendium.storage

import cats.effect.IO
import cats.syntax.apply._
import higherkindness.compendium.core.refinements.ProtocolId
import higherkindness.compendium.models.Protocol
import org.specs2.matcher.Matchers

class StorageStub(val proto: Option[Protocol], val identifier: ProtocolId)
    extends Storage[IO]
    with Matchers {
  override def store(id: ProtocolId, protocol: Protocol): IO[Unit] =
    IO {
      proto === Some(protocol)
      id === identifier
    } *> IO.unit

  override def recover(id: ProtocolId): IO[Option[Protocol]] =
    if (id == identifier) IO(proto) else IO(None)

  override def exists(id: ProtocolId): IO[Boolean] =
    IO(id == identifier)
}
