/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/js/**/*.js"
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        municipal: {
          bg: '#101827',
          sidebar: '#1e293b',
          card: '#202c3d',
          primary: '#1d4ed8',
          text: '#e2e8f0',
        }
      }
    },
  },
  plugins: [],
}
