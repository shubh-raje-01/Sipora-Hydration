import React, { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import { Plus, ShoppingBag, Loader2 } from 'lucide-react';
import { VESSELS as LOCAL_VESSELS, PODS as LOCAL_PODS } from '../constants';
import { useCart } from '../context/CartContext';
import { fetchAllProducts } from '../services/shopifyService';
import { Product } from '../types';

export default function ShopPage() {
  const { addToCart } = useCart();
  const [vessels, setVessels] = useState<Product[]>(LOCAL_VESSELS);
  const [pods, setPods] = useState<Product[]>(LOCAL_PODS);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadProducts() {
      const shopifyProducts = await fetchAllProducts();
      if (shopifyProducts.length > 0) {
        setVessels(shopifyProducts.filter(p => p.category === 'vessel'));
        setPods(shopifyProducts.filter(p => p.category === 'pod'));
      }
      setLoading(false);
    }
    loadProducts();
  }, []);

  return (
    <div className="pt-32 pb-20 px-8 max-w-7xl mx-auto">
      {/* Header */}
      <header className="py-12 md:py-20 flex flex-col items-start relative overflow-hidden mb-16">
        <div className="z-10 max-w-2xl">
          <motion.h1 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-5xl md:text-7xl font-bold tracking-tight text-on-surface mb-6"
          >
            Hydration <span className="text-primary italic">Perfected</span>.
          </motion.h1>
          <p className="text-lg md:text-xl text-on-surface-variant leading-relaxed mb-8">
            Select your Kinetic Vessel and pair it with our nutrient-dense flavor pods. Engineered for those who never stop moving.
          </p>
          {loading && (
            <div className="flex items-center gap-2 text-primary font-bold">
              <Loader2 className="w-5 h-5 animate-spin" />
              <span>Syncing with Shopify...</span>
            </div>
          )}
        </div>
        <div className="absolute -right-20 top-0 w-1/2 h-full opacity-10 pointer-events-none">
          <div className="w-full h-full bg-gradient-to-br from-primary to-tertiary blur-[100px] rounded-full"></div>
        </div>
      </header>

      {/* Vessel Selection */}
      <section className="mb-32">
        <div className="flex justify-between items-end mb-12">
          <div>
            <span className="inline-block px-4 py-1 bg-primary-container text-on-primary-container rounded-full text-[10px] font-bold tracking-widest uppercase mb-4">Step 01</span>
            <h2 className="text-4xl font-bold tracking-tight">Select Your Vessel</h2>
          </div>
          <div className="hidden md:block h-px flex-grow mx-12 bg-surface-container"></div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {vessels.map((vessel) => (
            <motion.div 
              key={vessel.id}
              whileHover={{ y: -10 }}
              className="group relative bg-surface-container-lowest rounded-[2.5rem] overflow-hidden transition-all duration-500 shadow-sm hover:shadow-xl"
            >
              <div className="aspect-[4/5] overflow-hidden bg-surface-container-low">
                <img 
                  src={vessel.image} 
                  alt={vessel.name}
                  className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                  referrerPolicy="no-referrer"
                />
              </div>
              <div className="p-10">
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-2xl font-bold">{vessel.name}</h3>
                  <span className="text-xl font-black text-primary">${vessel.price}</span>
                </div>
                <p className="text-on-surface-variant mb-6 font-medium leading-relaxed">{vessel.description}</p>
                <div className="flex gap-3 mb-8">
                  {vessel.colors?.map((color, i) => (
                    <div 
                      key={i} 
                      className="w-6 h-6 rounded-full border border-on-surface/10 ring-offset-2 transition-all cursor-pointer hover:ring-2 hover:ring-primary"
                      style={{ backgroundColor: color }}
                    />
                  ))}
                </div>
                <button 
                  onClick={() => addToCart(vessel)}
                  className="w-full py-5 bg-primary text-on-primary rounded-2xl font-bold text-lg hover:bg-primary-dim transition-all active:scale-95 shadow-lg shadow-primary/10"
                >
                  Add Vessel
                </button>
              </div>
            </motion.div>
          ))}
        </div>
      </section>

      {/* Flavor Pod Selection */}
      <section className="mb-32">
        <div className="flex items-center gap-6 mb-12">
          <span className="px-4 py-1 bg-secondary-container text-on-surface rounded-full text-[10px] font-bold tracking-widest uppercase">Step 02</span>
          <h2 className="text-4xl font-bold tracking-tight">Choose Your Infusion</h2>
          <div className="flex-grow h-px bg-surface-container"></div>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          {pods.map((pod) => (
            <motion.div 
              key={pod.id}
              whileHover={{ y: -10 }}
              className="bg-surface-container-low rounded-[2rem] p-8 border border-transparent hover:border-primary/10 transition-all group shadow-sm"
            >
              <div className="w-full aspect-square rounded-full bg-white flex items-center justify-center mb-8 overflow-hidden shadow-inner">
                <img 
                  src={pod.image} 
                  alt={pod.name}
                  className="w-full h-full object-cover mix-blend-multiply transition-transform group-hover:rotate-12 scale-90"
                  referrerPolicy="no-referrer"
                />
              </div>
              <h4 className="text-xl font-bold mb-1">{pod.name}</h4>
              <p className="text-sm text-on-surface-variant mb-6 font-medium">{pod.description}</p>
              <div className="flex justify-between items-center">
                <span className="font-black text-primary text-lg">${pod.price} <span className="text-xs text-on-surface-variant font-normal">/ 6pk</span></span>
                <button 
                  onClick={() => addToCart(pod)}
                  className="p-3 bg-white rounded-full text-primary hover:bg-primary hover:text-white transition-all shadow-sm active:scale-90"
                >
                  <Plus className="w-5 h-5" />
                </button>
              </div>
            </motion.div>
          ))}
        </div>
      </section>

      {/* Community Section */}
      <section className="bg-surface-container-highest rounded-[3rem] p-12 md:p-20 flex flex-col md:flex-row items-center justify-between gap-16 overflow-hidden relative shadow-xl">
        <div className="z-10 max-w-md">
          <h3 className="text-4xl font-bold mb-6 leading-tight">Never miss a drop.</h3>
          <p className="text-on-surface-variant text-lg mb-10 leading-relaxed">Join the Kinetic club for early access to limited edition vessels and flavor drops.</p>
          <div className="flex flex-col sm:flex-row gap-4">
            <input 
              type="email" 
              placeholder="Email address" 
              className="flex-grow px-8 py-5 rounded-2xl bg-white border-none focus:ring-2 focus:ring-primary/40 font-medium shadow-inner"
            />
            <button className="px-10 py-5 bg-on-surface text-white rounded-2xl font-bold hover:bg-primary transition-all active:scale-95 shadow-lg">
              Join
            </button>
          </div>
        </div>
        <div className="z-10 relative">
          <motion.div 
            animate={{ rotate: [3, -3, 3] }}
            transition={{ duration: 6, repeat: Infinity, ease: "easeInOut" }}
            className="bg-white p-3 rounded-[2rem] shadow-2xl"
          >
            <img 
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuAJgLpLsYMKoohbsJu33dME61CTK34-yANRF-Q8TaPUApssyj95CRaWZoLXPrZOWv9aP-MfjtoPwNHaunHjRSj8nnjsuQCx3U9JKhlRY3iK1J9PEQqfQPC9L3976IYib9ADnkMAY1Jm035Q2CNQlue3Q4NCTywQsjJ-TWi3vEYvD9f13964VdXbXmjf95SyI5HV1enXuv7NyP6F7eNSPQAGYnHwKoezIYBAXTFrUh20J0YP3pbuxldJkqj4GkOgnk6AdpBOhWxL4w" 
              alt="Lifestyle"
              className="w-72 h-96 object-cover rounded-[1.5rem]"
              referrerPolicy="no-referrer"
            />
          </motion.div>
          <div className="absolute -top-10 -right-10 bg-primary text-on-primary w-28 h-28 rounded-full flex items-center justify-center text-center leading-tight font-black -rotate-12 text-sm shadow-xl border-4 border-white">
            KINETIC<br/>CLUB
          </div>
        </div>
        <div className="absolute -bottom-20 -left-20 w-96 h-96 bg-primary/10 rounded-full blur-3xl"></div>
      </section>
    </div>
  );
}
