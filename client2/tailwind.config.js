const colors = require('tailwindcss/colors')

module.exports = {
    content: ['./src/**/*.{ts,tsx}'],
    theme: {
        fontFamily: {
            sans: ['Martian Mono', 'sans-serif']
        },
        extend: {
            boxShadow: {
                'centered': '0 0 10px 1px rgba(0,0,0,0.6)',
            },
            colors: {
                red: '#ff9194',
                pink: '#ffb4c1',
                green: '#00a28e',
                cyan: '#02a7b9',
                blue: '#04a2d9',

                dark: colors.gray['900'],
                light: '#faf9fe',
                lightHover: '#eeeeee',
            }
        },
    },
    plugins: [],
}