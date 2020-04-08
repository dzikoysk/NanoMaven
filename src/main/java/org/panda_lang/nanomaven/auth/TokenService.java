/*
 * Copyright (c) 2020 Dzikoysk
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

package org.panda_lang.nanomaven.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE_64_ENCODER = Base64.getUrlEncoder();
    public static final BCryptPasswordEncoder B_CRYPT_TOKENS_ENCODER = new BCryptPasswordEncoder();

    private final Map<String, Token> tokens = new HashMap<>();
    private final TokensStorage database;

    public TokenService() {
        this.database = new TokensStorage(this);
    }

    public void load() throws IOException {
        this.database.loadTokens();
    }

    public void save() throws IOException {
        this.database.saveTokens();
    }

    public String generateToken() {
        byte[] randomBytes = new byte[48];
        SECURE_RANDOM.nextBytes(randomBytes);
        return BASE_64_ENCODER.encodeToString(randomBytes);
    }

    public void addToken(Token token) {
        this.tokens.put(token.getAlias(), token);
    }

    public void deleteToken(String alias) {
        tokens.remove(alias);
    }

    public Token getToken(String alias) {
        return tokens.get(alias);
    }

    public int count() {
        return tokens.size();
    }

    public Collection<Token> getTokens() {
        return tokens.values();
    }

}
