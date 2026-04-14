import { Product } from "./types";

export const VESSELS: Product[] = [
  {
    id: "v1",
    name: "Titan Core 750",
    description: "Double-walled surgical steel with a kinetic-seal cap.",
    price: 49,
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuCYyWFVsJM-4DBb0tNv1rsPoYdpSXnCTjwqtNcXFnbZ2PvpUaJmh7Z5c4Est47zoMv4TGOw_UQCPIF5YlmyjINyxgnMTLsV9ZLKP5LXaXoz8RMW6rG2dOgN17Xu4fSxmU4ANV8SqCYk6ivOzUC5abcBf-TkxCWL47APPixjbNERXfdil7zsISH6bIF-0CzKyXIAPaazSRFkHIJM52A0XxD6qgczmRD4LWBvRlK-cf_y_Z2JctfhQefhUu-QmP9Nph2I-z0iY9nBhQ",
    category: "vessel",
    colors: ["#1c1917", "#a8a29e", "#ea580c"],
    tag: "BEST SELLER"
  },
  {
    id: "v2",
    name: "Element Glass",
    description: "Borosilicate glass with impact-resistant kinetic sleeve.",
    price: 35,
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuARgGvxlHh5-07DpzqgIPxyy2RiNiDLqQiiKcPeSkCWpBjr8JWAMjtJJbSs9_vglpYtCRQzJvpgSNrNAhXkHfq6vPoria38Cz0IgoOMhRrfXFVYbham3XieWCiIa0JBzHIBIEV99U6sCtJoWqWBvYRDKlcG3LMrhY1HJgSUYF8uFZZUFRk-Vt4U1s-uC1nR6AZy8UJlvNKaBtc0IYjrRmwsGFvY6DVt3vC_i5-VBzGKkptJpUZaxseow1dZdapvIVRuAUNjyOZs0g",
    category: "vessel",
    colors: ["#f5f5f4", "#3b82f6", "#292524"]
  },
  {
    id: "v3",
    name: "Apex Tactical",
    description: "Lightweight aerospace alloy with integrated pod-chamber.",
    price: 62,
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuDEunyB2I2rd4uPOMImUzApevKefAg1sv9dXn6nd7whALaro3nRULVXG1Q9JYKUkcrAcxwVb5KUisG3DdBR5CbCZ88EPJybpKJQxwGKbcd-PoFGT9jIbBaN_JoX1w4Hzfbo_jVSQKDBHaiJIFD-opog4n0Pg-qd--1aLKGkj-30-d_FoNUvmytgtRQo3QBatJeGMrB5sEVxHMlrgsMipH0YwESQ8TJgw8Updx5VqZU45fT0E0YwoQAulbEf_ILIVLOJ43Vdvf4wcw",
    category: "vessel",
    colors: ["#737373", "#059669", "#171717"]
  }
];

export const PODS: Product[] = [
  {
    id: "p1",
    name: "Blood Orange",
    description: "Electrolytes + B12",
    price: 12,
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuB1XgAPtnsXXuxJU2282JA1G4G3mPwGBt90flRUqt4DmPltEa4n5XxU42Edf7aBGdqRW4fodyrLh6KlapwunSWs9d-fTdz4mHMfyZ-h5vGEyinak4PAtGKqrHakb_3q2CWI7baiic0Bu6qHoymCYs_0LYBv3A7iCYD7Vz9v8LqaSRJrolGhd7DZZXNp28vSTFkRhqFSyavcI7RMxH7yiUDBxopUxknELomipaPe5Ff1wpWwG5h1sI-xdDGgvHjSQ-4GjHtI9HzY-w",
    category: "pod",
    tag: "BEST SELLER"
  },
  {
    id: "p2",
    name: "Wild Berry",
    description: "Antioxidants + Zinc",
    price: 12,
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuCB8pAmtBOh6SjcAXkTrdo7zhrI40wrLJkGxZJo1AOJhoHHvQ8_4lxAHnBINMYiPQeEBjb0JRkxwiMdh6yG_gm8_kUCMsAHePi6sZHbi6PDQ2DJyXKOTJrzJGfVcMgU1_cRu6a3PJMAq3jbcM-rc_9vtQi6UxmRFOcYfdtfcrtYrzCoymi2_tu9LIuDy0xFLZISB-9GB8WLs4gGvkS8wWunMBqqSy7JqblioqoMhIYlAxX_MkAu2IFxSCuHZ8vCbz6d4SjtmbG6cQ",
    category: "pod"
  },
  {
    id: "p3",
    name: "Mint Lime",
    description: "Focus + Magnesium",
    price: 12,
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuCuVFVYym06jRYW9DtH8ycESL8ax919doW4jKFPyKaGtBkFf0hyf3F6OJ8-Ozizt5uSGQALYCq1oYyT_gMNu9Stzw8wyBmbInhuI-2gk0Y-k7YShiCRkKL8mKlCDn1judkGfMUKdXSoVvB61eS2su7s9nYxmZklK-Jx1PiRJutXVCz1uDX0tmvp3WrYSuFA8s74v_kXyZhVmGgyGL6TLfVyQVjVbr99aFTpLUc62xFAvAkF7hxQRHw2rZPZFv5ZP22BFjBIlYrbcA",
    category: "pod",
    tag: "NEW"
  },
  {
    id: "p4",
    name: "Tropical Punch",
    description: "Immunity + Vitamin C",
    price: 12,
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuCNFc2Ko-dnrQK4p7pPBfkgTBp9piX_kvuHwgnO6g6tdg9uZl4iiBio4TPdC6dvTnjZETjdF4Z_vM5PKxuRb34CpMSvIaVDl4RW5hO-h5kHEvCtv6p8ctmOnWxdkCsxlmvHgsx4RKjPkWA3blBtrJuAOtu1W_ofg9tDdwyN7Ep1qtsGZ644evRRDMU0nF_TWsPm_3LKMZVA7Kjw3HRhP5DH2v1Ldunco8VTtwYoE1HZZtb-ryfJvQU4_kqfPg6jkbEx0I2wGEr4yw",
    category: "pod"
  }
];
