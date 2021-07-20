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
  <div v-bind:class="{ 'dark': theme.isDark }">
    <router-view class="min-h-screen dark:bg-black dark:text-white"/>
  </div>
</template>

<script>
import { defineComponent, onMounted } from 'vue'
import { useHead } from '@vueuse/head'
import useTheme from "./store/theme"

export default defineComponent({
  setup() {
    useHead({
      title: window.REPOSILITE_TITLE,
      description: window.REPOSILITE_DESCRIPTION
    })

    const { theme, fetchTheme } = useTheme()

    onMounted(() => {
      fetchTheme()
    })

    return {
      theme
    }
  }
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;500;600&display=swap');

html {
  @apply bg-gray-100;
}

#app {
  font-family: 'Open Sans', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.container {
  @apply px-10;
}

.active {
  @apply dark:border-white;
}

.bg-default {
  @apply bg-gray-100 dark:border-gray-900;
}
</style>
