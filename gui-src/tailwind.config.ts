import { heroui } from "@heroui/theme";
import colors from "tailwindcss/colors";
import type { Config } from "tailwindcss";
import typography from "@tailwindcss/typography";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/hooks/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/shared/**/*.{js,ts,jsx,tsx,mdx}",
    "./node_modules/@heroui/theme/dist/components/(date-picker|button|ripple|spinner|calendar|date-input|popover).js",
  ],
  theme: {
    extend: {
      backgroundImage: {
        "custom-gradient": "linear-gradient(to right, #F5A04C, #EE7601, #DA3C1F, #A91D43, #B90D80, #462683)",
      },
      boxShadow: {
        modalShadow: "2px 2px 2px rgba(0, 0, 0, 0.15);",
        tabShadow: "0 -1px 2px rgba(0, 0, 0, 0.15), 2px 0 2px rgba(0, 0, 0, 0.15);",
      },
      keyframes: {
        "fade-in": {
          "0%": { opacity: "0" },
          "100%": { opacity: "1" },
        },
        "bounce-in": {
          "0%": { transform: "scale(0.8)", opacity: "0" },
          "70%": { transform: "scale(1.1)" },
          "100%": { transform: "scale(1)", opacity: "1" },
        },
      },
      animation: {
        "fade-in": "fade-in 0.3s ease-in-out",
        "bounce-in": "bounce-in 0.5s ease-out",
      },
      colors: {
        grey: colors.gray,
      }
    },
    fontSize: {
      sm: "0.8rem",
      md: "1.25rem",
      xl: "1.5rem",
      "2xl": "1.875rem",
      "3xl": "2rem",
      "4xl": "2.25rem",
    },
    colors: {
      white: "#FFFFFF",
      black: "#000000",
      blue: "#0066cc",
      lightblue: "#e0eaf2",
      green: "#599C35",
      lightgrey: "#F5F5F5",
      grey_disabled: "#BFBFBF",
      grey: "#E0E0E0",
      grey_variant: "#6f6f6f",
      line_number: "#888888",
      purple: "#45327d",
      lightpurple: "#b48ead",
      magenta: "#B90D80",
      visited_link: "#8867CD",
      orange: "#EE7601",
      orange2: "#DA3C1F",
      red: "#A91D43",
      lightred: "#ffebeb",
      yellow: "#F5A04C",
      darkblue: "#00008b",
      bg_body: "#f6f8fa",
      dark_variation: "#45327d",
    },
    screens: {
      xs: "375px",
      sm: "640px",
      md: "768px",
      lg: "975px",
      xl: "1024px",
      "2xl": "1280px",
      "3xl": "1536px",
      "4k": "2560px",
    },
    typography: {
      DEFAULT: {
        css: {
          p: {
            textAlign: "justify",
            marginBottom: "0.5rem",
            marginTop: "0.5rem",
          },
          h1: {
            fontSize: "36px",
            color: colors.gray["900"],
          },
          h2: {
            fontSize: "28px",
            color: colors.gray["900"],
          },
          h3: {
            fontSize: "22px",
            color: colors.gray["900"],
          },
          h4: {
            fontSize: "15px",
            color: colors.gray["900"],
          },
          a: {
            color: colors.blue["600"],
            textDecoration: "underline",
            "&:hover": {
              color: colors.blue["800"],
            },
          },
          ul: {
            listStyleType: "disc",
            paddingLeft: "1.5rem",
            marginBottom: "0.5rem",
            marginTop: "0.5rem",
          },
          ol: {
            listStyleType: "decimal",
            paddingLeft: "1.5rem",
            marginBottom: "0.5rem",
            marginTop: "0.5rem",
          },
          table: {
            width: "100%",
            marginBottom: "0.5rem",
            marginTop: "0.5rem",
          },
          thead: {
            backgroundColor: colors.gray["50"],
          },
          "thead th": {
            backgroundColor: colors.gray["100"],
            padding: "0.75rem",
            fontWeight: "600",
          },
          "tbody td": {
            padding: "0.75rem",
          },
        },
      },
    },
  },
  plugins: [heroui(), typography],
};
export default config;
