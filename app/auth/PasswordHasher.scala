/**
  * Original work: SecureSocial (https://github.com/jaliss/securesocial)
  * Copyright 2013 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
  *
  * Derivative work: PlayAuthenticated (https://github.com/mslinn/play-authenticated)
  * Modifications Copyright 2017 Micronautics Research (sales@micronauticsresearch.com)
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
  * limitations under the License. */

package auth

import model.Password
import org.mindrot.jbcrypt.BCrypt

/** Implementation of the password hasher based on BCrypt.
  * @see [[http://www.mindrot.org/files/jBCrypt/jBCrypt-0.2-doc/BCrypt.html#gensalt(int) gensalt]] */
object PasswordHasher {
  /* The log2 of the number of rounds of hashing to apply. */
  val logRounds: Int = 10

  def hash(plainText: String): Password =
    Password(BCrypt.hashpw(plainText, BCrypt.gensalt(logRounds)))

  def matches(password: Password, suppliedPassword: Password): Boolean =
    BCrypt.checkpw(suppliedPassword.value, password.value)
}
