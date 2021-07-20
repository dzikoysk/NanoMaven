<!--
  - Copyright (c) 2021 dzikoysk
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <header class="bg-gray-100 dark:bg-black dark:text-white">
    <div class="container mx-auto flex flex-row py-10 justify-between">
      <h1 class="text-xl font-medium py-1">
        <router-link to="/">{{ title }}</router-link>
      </h1>
      <nav class="flex flex-row mt-0.5">
       <router-link to="/dashboard">
          <div class="py-1.5 rounded-full bg-white font-bold px-6 text-sm dark:border-gray-900">
            Dashboard
          </div>
        </router-link>
        <div class="ml-4 pl-2 pr-1 py-0.9 cursor-pointer rounded-full bg-white" v-on:click="toggleTheme()">
          <SunIcon v-if="theme.isDark"/>
          <MoonIcon v-else/>
        </div>
      </nav>
    </div>
  </header>
</template>

<script>
import MoonIcon from './icons/MoonIcon.vue'
import SunIcon from './icons/SunIcon.vue'
import useTheme from "../store/theme"

export default {
  name: 'Index',
  components: {
    MoonIcon,
    SunIcon
  },
  setup() {
    const { theme, toggleTheme } = useTheme()

    return {
      theme,
      toggleTheme
    }
  },
  data() {
    return {
      title: window.REPOSILITE_TITLE
    }
  }
}
</script>