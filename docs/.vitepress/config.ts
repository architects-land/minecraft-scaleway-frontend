import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Minecraft Scaleway Frontend",
  description: "Fake Minecraft Server used as a frontend to hourly paid Scaleway servers.",
  base: '/minecraft-scaleway-frontend/',
  lang: 'en-US',
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'How it works', link: '/how-it-works' },
      { text: 'Getting started', link: '/getting-started' },
    ],

    sidebar: [
      {
        text: 'Usage',
        items: [
          { text: 'How it works', link: '/how-it-works' },
          { text: 'Getting started', link: '/getting-started' },
          { text: 'Maintain', link: '/maintain' },
          { text: 'Advanced configuration', link: '/advanced-configuration' },
          { text: 'Server Stopper', link: '/server-stopper' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/architects-land/minecraft-scaleway-frontend' }
    ],

    footer: {
      message: 'Released under the AGPL License. <a href="https://www.anhgelus.world/legal/" target="_blank">Legal information</a>.',
      copyright: 'Copyright © 2025 Architects Land and William Hergès'
    }
  }
})
