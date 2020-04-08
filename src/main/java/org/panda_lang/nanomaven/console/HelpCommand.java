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

package org.panda_lang.nanomaven.console;

import org.panda_lang.nanomaven.NanoMaven;
import org.panda_lang.nanomaven.NanoConstants;

public class HelpCommand implements NanoCommand {

    @Override
    public void call(NanoMaven nanoMaven) {
        NanoMaven.getLogger().info("");
        NanoMaven.getLogger().info("NanoMaven " + NanoConstants.VERSION + " Commands:");
        NanoMaven.getLogger().info("  help - List available commands");
        NanoMaven.getLogger().info("  tokens - List all generated tokens");
        NanoMaven.getLogger().info("  keygen <path> <alias> - Generate a new access token for the given path");
        NanoMaven.getLogger().info("  rs - Reinstall all artifacts");
        NanoMaven.getLogger().info("  stop - Shutdown server");
        NanoMaven.getLogger().info("");
    }

}
