import React, { useEffect, useState } from 'react';
import { motion, useScroll, useTransform } from 'motion/react';
import { Plus, Loader2 } from 'lucide-react';
import { VESSELS as LOCAL_VESSELS, PODS as LOCAL_PODS } from '../constants';
import { useCart } from '../context/CartContext';
import { fetchAllProducts } from '../services/shopifyService';
import { Product } from '../types';
import { cn } from '../lib/utils';

function ShopScrollytelling({ vessel, pod }: { vessel: Product; pod: Product }) {
  const ref = React.useRef<HTMLElement | null>(null);
  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ['start end', 'end start'],
  });
  const bottleY = useTransform(scrollYProgress, [0, 0.5, 1], [90, -20, -110]);
  const bottleRotate = useTransform(scrollYProgress, [0, 0.45, 1], [-10, 8, -6]);
  const bottleScale = useTransform(scrollYProgress, [0, 0.5, 1], [0.86, 1.05, 0.94]);
  const podX = useTransform(scrollYProgress, [0.1, 0.55, 0.9], [180, 8, -120]);
  const podRotate = useTransform(scrollYProgress, [0, 1], [18, -28]);
  const copyY = useTransform(scrollYProgress, [0, 0.45, 1], [40, -12, -40]);
  const mistOpacity = useTransform(scrollYProgress, [0.15, 0.55, 0.9], [0.1, 0.72, 0.18]);

  return (
    <section ref={ref} className="relative mb-32 min-h-[125vh] overflow-hidden rounded-lg bg-primary text-white">
      <div className="liquid-ether absolute inset-0 opacity-20" />
      <div className="sticky top-20 grid min-h-[calc(100vh-5rem)] items-center gap-10 px-6 py-16 md:px-12 lg:grid-cols-[0.86fr_1.14fr]">
        <motion.div style={{ y: copyY }} className="relative z-10 max-w-xl">
          <p className="mb-5 text-sm font-black uppercase tracking-[0.26em] text-secondary">Scroll to pair</p>
          <h2 className="text-5xl font-black leading-none tracking-tight md:text-7xl">Bottle first. Pod next. Habit follows.</h2>
          <p className="mt-8 text-lg font-semibold leading-8 text-white/72">
            The shopping flow should feel like assembly: choose the vessel, bring the pod into the sip path, then make the add-to-cart action obvious.
          </p>
        </motion.div>

        <div className="relative min-h-[36rem]">
          <motion.div
            style={{ opacity: mistOpacity }}
            className="absolute left-1/2 top-1/2 h-80 w-80 -translate-x-1/2 -translate-y-1/2 rounded-full bg-secondary blur-3xl"
          />
          <motion.div
            style={{ y: bottleY, rotate: bottleRotate, scale: bottleScale }}
            className="absolute left-[46%] top-1/2 h-[31rem] w-[16rem] -translate-x-1/2 -translate-y-1/2 overflow-hidden rounded-[4rem] border border-white/50 bg-white/10 p-5 shadow-2xl shadow-black/30"
          >
            <img src={vessel.image} alt={vessel.name} className="h-full w-full rounded-[3rem] object-cover" referrerPolicy="no-referrer" />
          </motion.div>
          <motion.div
            style={{ x: podX, rotate: podRotate }}
            className="absolute left-[57%] top-[44%] h-36 w-36 -translate-y-1/2 overflow-hidden rounded-full border-8 border-white/50 bg-secondary shadow-2xl"
          >
            <img src={pod.image} alt={pod.name} className="h-full w-full scale-125 object-cover mix-blend-multiply" referrerPolicy="no-referrer" />
          </motion.div>
        </div>
      </div>
    </section>
  );
}

function ProductCard({ product, onAdd }: { product: Product; onAdd: (product: Product) => void }) {
  const isBottle = product.category === 'vessel';

  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-80px' }}
      whileHover={{ y: -14, rotateX: isBottle ? 4 : 2, rotateY: isBottle ? -4 : 5 }}
      transition={{ type: 'spring', stiffness: 220, damping: 22 }}
      className={cn(
        'group relative overflow-hidden rounded-lg border border-white/70 bg-white p-5 shadow-sm transition-shadow duration-500 hover:shadow-2xl hover:shadow-primary/15 [transform-style:preserve-3d]',
        isBottle ? 'md:p-6' : 'p-5'
      )}
    >
      <div className="pointer-events-none absolute inset-0 opacity-0 transition duration-500 group-hover:opacity-100 card-shine" />
      <div className={cn('relative overflow-hidden rounded-lg', isBottle ? 'aspect-[4/5] bg-secondary-container' : 'aspect-square bg-tertiary-container')}>
        <motion.div
          className={cn('absolute inset-x-8 bottom-6 z-10 h-20 rounded-full blur-2xl', isBottle ? 'bg-secondary/60' : 'bg-tertiary/60')}
          animate={{ scale: [0.9, 1.08, 0.9], opacity: [0.32, 0.72, 0.32] }}
          transition={{ duration: isBottle ? 3.8 : 2.7, repeat: Infinity, ease: 'easeInOut' }}
        />
        <img
          src={product.image}
          alt={product.name}
          className={cn(
            'relative z-10 h-full w-full object-cover transition duration-700',
            isBottle ? 'group-hover:scale-110 group-hover:-rotate-3' : 'scale-90 rounded-full mix-blend-multiply group-hover:scale-105 group-hover:rotate-12'
          )}
          referrerPolicy="no-referrer"
        />
        <span className="absolute left-4 top-4 z-20 rounded-full bg-primary/90 px-3 py-1 text-[0.63rem] font-black uppercase tracking-[0.18em] text-white">
          {isBottle ? 'Bottle' : 'Pod'}
        </span>
      </div>

      <div className="pt-6">
        <div className="mb-2 flex items-start justify-between gap-4">
          <h3 className={cn('font-black tracking-tight', isBottle ? 'text-2xl' : 'text-xl')}>{product.name}</h3>
          <span className="rounded-full bg-primary px-3 py-2 text-sm font-black text-white">${product.price}</span>
        </div>
        <p className="mb-6 text-sm font-semibold leading-6 text-on-surface-variant">{product.description}</p>
        {product.colors && (
          <div className="mb-6 flex gap-3">
            {product.colors.map((color) => (
              <span key={color} className="h-6 w-6 rounded-full border border-on-surface/10 ring-offset-2 transition group-hover:ring-2 group-hover:ring-secondary" style={{ backgroundColor: color }} />
            ))}
          </div>
        )}
        <button
          onClick={() => onAdd(product)}
          className="flex w-full items-center justify-center gap-2 rounded-full bg-primary px-5 py-4 text-sm font-black text-white shadow-lg shadow-primary/10 transition active:scale-95 group-hover:bg-primary-dim"
        >
          {isBottle ? 'Add Vessel' : 'Add Pod'}
          <Plus className="h-4 w-4" />
        </button>
      </div>
    </motion.div>
  );
}

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

      <ShopScrollytelling vessel={vessels[0] ?? LOCAL_VESSELS[0]} pod={pods[0] ?? LOCAL_PODS[0]} />

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
            <ProductCard key={vessel.id} product={vessel} onAdd={addToCart} />
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
            <ProductCard key={pod.id} product={pod} onAdd={addToCart} />
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
