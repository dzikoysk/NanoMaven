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

import org.panda_lang.nanomaven.NanoConstants;
import org.panda_lang.nanomaven.NanoMaven;
import org.panda_lang.nanomaven.utils.YamlUtils;

import java.io.File;
import java.io.IOException;

public class TokensStorage {

    private static final File TOKENS_FILE = new File(NanoConstants.TOKENS_FILE_NAME);

    private final TokenService tokenService;

    public TokensStorage(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public void loadTokens() throws IOException {
        TokensCollection tokensCollection = YamlUtils.load(TOKENS_FILE, TokensCollection.class);

        for (Token token : tokensCollection.getTokens()) {
            tokenService.addToken(token);
        }

        NanoMaven.getLogger().info("Loaded tokens: " + tokenService.count());
    }

    public void saveTokens() throws IOException {
        TokensCollection tokensCollection = YamlUtils.load(TOKENS_FILE, TokensCollection.class);

        for (Token token : tokenService.getTokens()) {
            tokensCollection.getTokens().add(token);
        }

        YamlUtils.save(TOKENS_FILE, tokensCollection);
        NanoMaven.getLogger().info("Saved tokens: " + tokensCollection.getTokens().size());
    }

}
