import type { Config } from "tailwindcss";

/**
 * Sipora Design Token System
 * ─────────────────────────────────────────────────────────────────
 * Color palette:
 *   Ice Chip         #F3F6F6   primary background — cool, clean
 *   Celestial Powder #A3E4FA   light blue — hero sections, highlights
 *   Marshmallow Blue #90D6F9   mid blue — hover states, Aire AI
 *   Cotton Candy     #FFBCD9   soft pink — emotional accents, promotions
 *   Pink Ciclet      #FFA8C5   deeper pink — CTAs, badges, hover on pink
 *   Black Box        #0F282F   deep teal-black — text, navbar, dark sections
 *
 * Typography:
 *   Display: Fraunces (optical-size serif — warm, distinctive)
 *   Body:    DM Sans  (geometric humanist — clean, readable)
 *
 * Both loaded in index.html via Google Fonts.
 */
const config: Config = {
    content: ["./index.html", "./src/**/*.{ts,tsx}"],

    theme: {
        extend: {

            // ── Colors ==>
            colors: {
                // Sipora core palette
                ice:       "#F3F6F6",   // Ice Chip — page background
                celestial: "#A3E4FA",   // Celestial Powder — hero tints
                marshmallow: "#90D6F9", // Marshmallow Blue — Aire AI accent
                cotton:    "#FFBCD9",   // Cotton Candy — soft pink surfaces
                ciclet:    "#FFA8C5",   // Pink Ciclet — CTAs, active states
                blackbox:  "#0F282F",   // Black Box    — headings, nav, dark bg

                // Extended shades derived from the palette
                // Use when you need lighter/darker variants without leaving the system
                "ice-dark":       "#E3EAEB",
                "celestial-dark": "#6BCFF5",
                "marshmallow-dark": "#5DC0F2",
                "cotton-dark":    "#FF9DC4",
                "ciclet-dark":    "#FF7AAD",
                "blackbox-light": "#1A3D47",

                // Semantic aliases — use these in components, not raw hex
                // Makes it trivial to retheme without touching every component
                brand: {
                    DEFAULT: "#0F282F",    // blackbox
                    light:   "#1A3D47",
                },
                surface: {
                    DEFAULT: "#F3F6F6",    // ice
                    card:    "#FAFBFB",    // slightly lighter for card bg
                    dark:    "#0F282F",    // blackbox for dark sections
                },
                accent: {
                    blue:     "#90D6F9",   // marshmallow — Aire AI, info
                    "blue-light": "#A3E4FA", // celestial
                    pink:     "#FFA8C5",   // ciclet — CTAs, badges
                    "pink-light": "#FFBCD9", // cotton — soft pink bg
                },
                text: {
                    DEFAULT:   "#0F282F",  // blackbox — primary text
                    muted:     "#4A6770",  // desaturated for secondary text
                    light:     "#7A9BA5",  // for placeholders, disabled
                    inverse:   "#F3F6F6",  // ice — text on dark backgrounds
                },
                border: {
                    DEFAULT: "#D8E4E6",    // light grey-blue for card borders
                    strong:  "#A3BCBF",    // stronger border
                },

                // Status colours — kept accessible
                success:  "#1D8A55",
                "success-bg": "#E6F5EE",
                warning:  "#B07B1A",
                "warning-bg": "#FDF5E0",
                error:    "#C13232",
                "error-bg": "#FCECEC",
            },

            fontFamily: {
                // Display / headings — Fraunces
                display: ["Fraunces", "Georgia", "serif"],
                // Body / UI — DM Sans
                sans:    ["DM Sans", "system-ui", "sans-serif"],
                // Monospace — for prices, codes
                mono:    ["DM Mono", "ui-monospace", "monospace"],
            },

            fontSize: {
                // Display sizes — used with font-display
                "display-2xl": ["4.5rem",  { lineHeight: "1.1", letterSpacing: "-0.03em" }],
                "display-xl":  ["3.75rem", { lineHeight: "1.1", letterSpacing: "-0.02em" }],
                "display-lg":  ["3rem",    { lineHeight: "1.15", letterSpacing: "-0.02em" }],
                "display-md":  ["2.25rem", { lineHeight: "1.2",  letterSpacing: "-0.01em" }],
                "display-sm":  ["1.875rem",{ lineHeight: "1.25", letterSpacing: "-0.01em" }],
                "display-xs":  ["1.5rem",  { lineHeight: "1.3" }],

                // Body sizes — used with font-sans
                "body-xl": ["1.25rem",  { lineHeight: "1.7" }],
                "body-lg": ["1.125rem", { lineHeight: "1.7" }],
                "body-md": ["1rem",     { lineHeight: "1.65" }],
                "body-sm": ["0.875rem", { lineHeight: "1.6" }],
                "body-xs": ["0.75rem",  { lineHeight: "1.55" }],
            },

            spacing: {
                // Extra large spacing tokens for section padding
                "18":  "4.5rem",
                "22":  "5.5rem",
                "26":  "6.5rem",
                "30":  "7.5rem",
                "34":  "8.5rem",
                "38":  "9.5rem",
                "42":  "10.5rem",
                "section": "6rem",    // standard section vertical padding
            },

            borderRadius: {
                "4xl": "2rem",
                "5xl": "2.5rem",
                "pill": "9999px",
            },

            boxShadow: {
                // Wellness-forward — soft, warm shadows (not the cold grey defaults)
                "soft-sm":  "0 2px 8px rgba(15, 40, 47, 0.06)",
                "soft-md":  "0 4px 16px rgba(15, 40, 47, 0.08)",
                "soft-lg":  "0 8px 32px rgba(15, 40, 47, 0.10)",
                "soft-xl":  "0 16px 48px rgba(15, 40, 47, 0.12)",
                "soft-2xl": "0 24px 64px rgba(15, 40, 47, 0.14)",

                // Blue glow — for Aire AI chat elements
                "glow-blue": "0 0 24px rgba(144, 214, 249, 0.50)",
                "glow-blue-lg": "0 0 48px rgba(144, 214, 249, 0.40)",

                // Pink glow — for CTAs and active states
                "glow-pink": "0 0 24px rgba(255, 168, 197, 0.50)",

                // Card hover lift
                "hover":  "0 12px 40px rgba(15, 40, 47, 0.13)",
            },

            backgroundImage: {
                // Hero gradient — ice to celestial
                "hero":         "linear-gradient(135deg, #F3F6F6 0%, #A3E4FA 50%, #FFBCD9 100%)",
                // Aire AI chat background
                "aire":         "linear-gradient(160deg, #0F282F 0%, #1A3D47 50%, #0F282F 100%)",
                // Subtle section separators
                "section-fade": "linear-gradient(180deg, #F3F6F6 0%, transparent 100%)",
                // CTA button gradient
                "btn-primary":  "linear-gradient(135deg, #0F282F 0%, #1A3D47 100%)",
                "btn-accent":   "linear-gradient(135deg, #FFA8C5 0%, #FFBCD9 100%)",
            },

            keyframes: {
                "fade-up": {
                    "0%":   { opacity: "0", transform: "translateY(20px)" },
                    "100%": { opacity: "1", transform: "translateY(0)" },
                },
                "fade-in": {
                    "0%":   { opacity: "0" },
                    "100%": { opacity: "1" },
                },
                "slide-in-right": {
                    "0%":   { transform: "translateX(100%)" },
                    "100%": { transform: "translateX(0)" },
                },
                "slide-out-right": {
                    "0%":   { transform: "translateX(0)" },
                    "100%": { transform: "translateX(100%)" },
                },
                "scale-in": {
                    "0%":   { opacity: "0", transform: "scale(0.95)" },
                    "100%": { opacity: "1", transform: "scale(1)" },
                },
                // Water ripple for hero section
                "ripple": {
                    "0%":   { transform: "scale(1)", opacity: "0.6" },
                    "100%": { transform: "scale(2.5)", opacity: "0" },
                },
                // Typing cursor blink for Aire AI
                "cursor-blink": {
                    "0%, 100%": { opacity: "1" },
                    "50%":       { opacity: "0" },
                },
                // Gentle float for product images
                "float": {
                    "0%, 100%": { transform: "translateY(0px)" },
                    "50%":       { transform: "translateY(-10px)" },
                },
            },
            animation: {
                "fade-up":         "fade-up 0.5s ease-out forwards",
                "fade-up-delay-1": "fade-up 0.5s 0.1s ease-out forwards",
                "fade-up-delay-2": "fade-up 0.5s 0.2s ease-out forwards",
                "fade-up-delay-3": "fade-up 0.5s 0.3s ease-out forwards",
                "fade-in":         "fade-in 0.4s ease-out forwards",
                "slide-in-right":  "slide-in-right 0.35s cubic-bezier(0.32, 0, 0.15, 1)",
                "slide-out-right": "slide-out-right 0.3s cubic-bezier(0.32, 0, 0.15, 1)",
                "scale-in":        "scale-in 0.25s ease-out",
                "ripple":          "ripple 2s ease-out infinite",
                "cursor-blink":    "cursor-blink 1s step-end infinite",
                "float":           "float 4s ease-in-out infinite",
            },

            transitionTimingFunction: {
                "spring":   "cubic-bezier(0.34, 1.56, 0.64, 1)",
                "smooth":   "cubic-bezier(0.4, 0, 0.2, 1)",
                "entrance": "cubic-bezier(0, 0, 0.2, 1)",
                "exit":     "cubic-bezier(0.4, 0, 1, 1)",
            },
        },
    },

    plugins: [],
};

export default config;