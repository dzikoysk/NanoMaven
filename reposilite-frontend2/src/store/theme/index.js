/*
 * Copyright (c) 2021 dzikoysk
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

import { reactive, toRef, toRefs } from "vue"

const themeKey = 'dark-theme'
const theme = reactive({
  isDark: false
})

export default function useTheme() {
  const fetchTheme = () => {
    theme.isDark = (localStorage.getItem(themeKey) === 'true')
  }

  const toggleTheme = () => {
    theme.isDark = !theme.isDark
    localStorage.setItem(themeKey, theme.isDark)
  }

  return {
    theme,
    fetchTheme,
    toggleTheme
  }
}