import React from 'react';
import { motion, useScroll, useTransform } from 'motion/react';
import { ArrowRight, Droplets, Leaf, Sparkles, Wind } from 'lucide-react';
import { Link } from 'react-router-dom';
import { PODS, VESSELS } from '../constants';
import { Product } from '../types';

const stackCards = [
  {
    kicker: 'Step 01',
    title: 'Fill with cold water.',
    body: 'Start with plain water. No syrup, sugar, or fizz required.',
    color: 'bg-secondary-container',
  },
  {
    kicker: 'Step 02',
    title: 'Snap on a Sipora pod.',
    body: 'Aroma travels with each sip and turns neutral water into a flavor cue.',
    color: 'bg-tertiary-container',
  },
  {
    kicker: 'Step 03',
    title: 'Your brain finishes the flavor.',
    body: 'Retronasal smell does the heavy lifting, so the bottle stays clean and light.',
    color: 'bg-primary text-white',
  },
];

const proofPoints = [
  { label: '0g', detail: 'sugar' },
  { label: '5L', detail: 'per pod' },
  { label: '12+', detail: 'flavor moods' },
];

function ScrollBottle() {
  const { scrollYProgress } = useScroll();
  const rotate = useTransform(scrollYProgress, [0, 1], [-18, 22]);
  const lift = useTransform(scrollYProgress, [0, 1], [28, -36]);
  const cap = useTransform(scrollYProgress, [0, 1], [0, -18]);

  return (
    <motion.div
      style={{ rotate, y: lift }}
      className="relative mx-auto h-[31rem] w-[14rem] max-sm:h-[24rem] max-sm:w-[11rem]"
      aria-hidden="true"
    >
      <motion.div
        style={{ y: cap }}
        className="absolute left-1/2 top-0 h-16 w-24 -translate-x-1/2 rounded-t-[2rem] rounded-b-xl bg-primary shadow-xl"
      />
      <div className="absolute left-1/2 top-12 h-[25rem] w-[11rem] -translate-x-1/2 rounded-[4rem] border border-white/70 bg-gradient-to-br from-white/90 via-secondary/75 to-tertiary-container/95 shadow-2xl shadow-primary/20 max-sm:h-[19rem] max-sm:w-[8.5rem]">
        <div className="absolute inset-x-7 top-10 h-52 rounded-full bg-white/38 blur-xl" />
        <div className="absolute left-1/2 top-24 h-28 w-16 -translate-x-1/2 rounded-full bg-primary/90 text-center text-xs font-black uppercase tracking-[0.26em] text-white [writing-mode:vertical-rl] flex items-center justify-center">
          Sipora
        </div>
        <div className="absolute bottom-8 left-1/2 h-16 w-16 -translate-x-1/2 rounded-full border-8 border-white/60 bg-secondary" />
      </div>
    </motion.div>
  );
}

function BottleModelViewer() {
  const { scrollYProgress } = useScroll();
  const spin = useTransform(scrollYProgress, [0, 0.55], [-16, 20]);
  const float = useTransform(scrollYProgress, [0, 0.55], [0, -70]);
  const glow = useTransform(scrollYProgress, [0, 0.55], [0.28, 0.55]);

  return (
    <motion.div
      className="pointer-events-none absolute right-[-6rem] top-20 z-0 hidden h-[48rem] w-[34rem] items-center justify-center opacity-70 lg:flex"
      style={{ y: float }}
      aria-hidden="true"
    >
      <div className="absolute inset-12 rounded-[42%] bg-white/20 blur-3xl" />
      <motion.div
        className="relative h-[43rem] w-[18rem] [perspective:1200px]"
        style={{ rotateY: spin, rotateZ: -7 }}
      >
        <motion.div
          className="absolute left-1/2 top-0 h-24 w-28 -translate-x-1/2 rounded-t-[2.4rem] rounded-b-xl bg-primary shadow-2xl"
          animate={{ y: [0, -10, 0] }}
          transition={{ duration: 4.5, repeat: Infinity, ease: 'easeInOut' }}
        />
        <div className="absolute left-1/2 top-16 h-[36rem] w-[14rem] -translate-x-1/2 overflow-hidden rounded-[5rem] border border-white/70 bg-gradient-to-br from-white/90 via-secondary/80 to-tertiary-container shadow-[0_32px_90px_rgba(15,40,47,0.28)]">
          <motion.div className="absolute inset-x-5 top-8 h-80 rounded-full bg-white/35 blur-2xl" style={{ opacity: glow }} />
          <div className="absolute left-8 top-16 h-[28rem] w-8 rounded-full bg-white/42 blur-md" />
          <div className="absolute right-8 top-24 h-72 w-6 rounded-full bg-primary/10 blur-sm" />
          <div className="absolute left-1/2 top-44 flex h-40 w-20 -translate-x-1/2 items-center justify-center rounded-full bg-primary text-xs font-black uppercase tracking-[0.28em] text-white [writing-mode:vertical-rl]">
            Sipora
          </div>
          <div className="absolute bottom-12 left-1/2 h-24 w-24 -translate-x-1/2 rounded-full border-[0.7rem] border-white/65 bg-secondary/90 shadow-inner" />
        </div>
      </motion.div>
    </motion.div>
  );
}

function MagicProductCard({ product, index }: { product: Product; index: number }) {
  const [spot, setSpot] = React.useState({ x: '50%', y: '50%' });

  return (
    <motion.article
      initial={{ opacity: 0, y: 28 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-80px' }}
      transition={{ delay: index * 0.08 }}
      onMouseMove={(event) => {
        const rect = event.currentTarget.getBoundingClientRect();
        setSpot({
          x: `${event.clientX - rect.left}px`,
          y: `${event.clientY - rect.top}px`,
        });
      }}
      whileHover={{ y: -8, rotateX: 3, rotateY: -3 }}
      className="group relative overflow-hidden rounded-lg border border-white/70 bg-white/78 p-5 shadow-xl shadow-primary/10 [transform-style:preserve-3d]"
    >
      <div className="card-shine pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-500 group-hover:opacity-100" style={{ '--x': spot.x, '--y': spot.y } as React.CSSProperties} />
      <div className="relative aspect-[4/5] overflow-hidden rounded-lg bg-secondary-container">
        <img
          src={product.image}
          alt={product.name}
          className="h-full w-full object-cover transition duration-700 group-hover:scale-110"
          referrerPolicy="no-referrer"
        />
        {product.tag && (
          <span className="absolute left-4 top-4 rounded-full bg-white/90 px-3 py-1 text-[0.65rem] font-black uppercase tracking-[0.18em] text-primary">
            {product.tag}
          </span>
        )}
      </div>
      <div className="relative pt-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h3 className="text-2xl font-black tracking-tight">{product.name}</h3>
            <p className="mt-1 text-sm font-semibold text-on-surface-variant">{product.description}</p>
          </div>
          <span className="rounded-full bg-primary px-3 py-2 text-sm font-black text-white">${product.price}</span>
        </div>
        <button className="mt-6 flex w-full items-center justify-center gap-2 rounded-full bg-surface-container-high px-5 py-4 text-sm font-black text-primary transition group-hover:bg-primary group-hover:text-white">
          Quick view <ArrowRight className="h-4 w-4" />
        </button>
      </div>
    </motion.article>
  );
}

export default function LandingPage() {
  const featured = [...VESSELS.slice(0, 2), ...PODS.slice(0, 2)];

  return (
    <div className="w-full overflow-hidden">
      <section className="hero-grid-bg relative min-h-screen px-5 pt-28 md:px-8">
        <div className="liquid-ether absolute inset-0 overflow-hidden opacity-95" />
        <BottleModelViewer />
        <div className="absolute inset-0 z-0 bg-[linear-gradient(90deg,rgba(243,246,246,0.96)_0%,rgba(243,246,246,0.82)_45%,rgba(243,246,246,0.24)_100%)]" />
        <div className="relative z-10 mx-auto grid max-w-7xl grid-cols-1 items-center gap-8 lg:grid-cols-[1.05fr_0.95fr]">
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7 }}
            className="relative z-10"
          >
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-primary/10 bg-white/80 px-4 py-2 text-xs font-black uppercase tracking-[0.2em] text-primary">
              <Sparkles className="h-4 w-4 text-tertiary" />
              Aroma-led hydration
            </div>
            <h1 className="max-w-4xl text-6xl font-black leading-[0.88] tracking-tight text-primary md:text-8xl lg:text-9xl">
              Sipora makes water feel new.
            </h1>
            <p className="mt-8 max-w-2xl text-lg font-semibold leading-8 text-on-surface-variant md:text-2xl">
              A playful scent-pod bottle for people who want more water without sugar, additives, or boring desk-bottle energy.
            </p>
            <div className="mt-10 flex flex-wrap gap-4">
              <Link to="/shop" className="kinetic-gradient inline-flex items-center gap-3 rounded-full px-8 py-4 text-base font-black text-white shadow-xl shadow-primary/20 transition hover:-translate-y-1">
                Explore bottles <ArrowRight className="h-5 w-5" />
              </Link>
              <a href="#how-it-works" className="inline-flex items-center gap-3 rounded-full bg-white px-8 py-4 text-base font-black text-primary shadow-lg shadow-primary/10 transition hover:-translate-y-1">
                See the science
              </a>
            </div>
          </motion.div>

          <div className="grid min-h-[36rem] grid-cols-2 grid-rows-[1fr_0.8fr_1fr] gap-4 max-sm:min-h-[30rem]">
            <motion.div className="rounded-lg bg-secondary p-5 shadow-xl shadow-primary/10" initial={{ opacity: 0, y: 28 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
              <Droplets className="mb-8 h-9 w-9 text-primary" />
              <p className="text-3xl font-black leading-none text-primary">Plain water, brighter ritual.</p>
            </motion.div>
            <motion.div className="row-span-2 overflow-hidden rounded-lg bg-tertiary-container shadow-xl shadow-primary/10" initial={{ opacity: 0, y: 28 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
              <img src={VESSELS[0].image} alt="Sipora bottle" className="h-full w-full object-cover" referrerPolicy="no-referrer" />
            </motion.div>
            <motion.div className="overflow-hidden rounded-lg bg-white p-4 shadow-xl shadow-primary/10" initial={{ opacity: 0, y: 28 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
              <ScrollBottle />
            </motion.div>
            <motion.div className="rounded-lg bg-primary p-5 text-white shadow-xl shadow-primary/10" initial={{ opacity: 0, y: 28 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4 }}>
              <Wind className="mb-8 h-9 w-9 text-secondary" />
              <p className="text-3xl font-black leading-none">Taste through scent.</p>
            </motion.div>
            <motion.div className="rounded-lg bg-tertiary p-5 shadow-xl shadow-primary/10" initial={{ opacity: 0, y: 28 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.5 }}>
              <Leaf className="mb-8 h-9 w-9 text-primary" />
              <p className="text-3xl font-black leading-none text-primary">Zero sugar. Full cue.</p>
            </motion.div>
          </div>
        </div>
      </section>

      <section className="bg-primary py-5 text-white">
        <div className="flex min-w-max animate-[marquee_24s_linear_infinite] gap-12 text-2xl font-black uppercase tracking-[0.18em]">
          {Array.from({ length: 8 }).map((_, index) => (
            <span key={index}>Water first / scent second / sugar never</span>
          ))}
        </div>
      </section>

      <section id="how-it-works" className="relative px-5 py-28 md:px-8">
        <div className="mx-auto grid max-w-7xl gap-12 lg:grid-cols-[0.82fr_1.18fr]">
          <div className="top-28 h-fit lg:sticky">
            <p className="mb-4 text-sm font-black uppercase tracking-[0.22em] text-tertiary">Scroll stack</p>
            <h2 className="text-5xl font-black leading-none tracking-tight text-primary md:text-7xl">How Sipora changes the sip.</h2>
            <p className="mt-6 text-lg font-semibold leading-8 text-on-surface-variant">
              The page borrows air-up's product storytelling pattern, but uses Sipora's own grid, pastel clarity, and startup-like motion language.
            </p>
          </div>
          <div className="space-y-8">
            {stackCards.map((card, index) => (
              <motion.article
                key={card.title}
                initial={{ opacity: 0, y: 80, rotate: -2 }}
                whileInView={{ opacity: 1, y: 0, rotate: 0 }}
                viewport={{ once: false, margin: '-120px' }}
                transition={{ duration: 0.55 }}
                className={`${card.color} sticky top-24 min-h-[26rem] rounded-lg p-8 shadow-2xl shadow-primary/10 md:p-12`}
                style={{ marginTop: index === 0 ? 0 : '8rem' }}
              >
                <p className="text-sm font-black uppercase tracking-[0.26em] opacity-70">{card.kicker}</p>
                <h3 className="mt-8 max-w-2xl text-5xl font-black leading-none tracking-tight md:text-7xl">{card.title}</h3>
                <p className="mt-8 max-w-xl text-xl font-semibold leading-8 opacity-80">{card.body}</p>
              </motion.article>
            ))}
          </div>
        </div>
      </section>

      <section className="bg-white px-5 py-28 md:px-8">
        <div className="mx-auto max-w-7xl">
          <div className="mb-14 flex flex-col justify-between gap-6 md:flex-row md:items-end">
            <div>
              <p className="mb-4 text-sm font-black uppercase tracking-[0.22em] text-tertiary">Magic bento</p>
              <h2 className="max-w-3xl text-5xl font-black leading-none tracking-tight text-primary md:text-7xl">Products that react before they sell.</h2>
            </div>
            <Link to="/shop" className="inline-flex items-center gap-2 text-base font-black text-primary">
              View shop <ArrowRight className="h-5 w-5" />
            </Link>
          </div>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {featured.map((product, index) => (
              <MagicProductCard key={product.id} product={product} index={index} />
            ))}
          </div>
        </div>
      </section>

      <section className="px-5 py-28 md:px-8">
        <div className="mx-auto grid max-w-7xl gap-8 lg:grid-cols-[1fr_0.8fr]">
          <div className="rounded-lg bg-secondary-container p-8 md:p-12">
            <h2 className="max-w-3xl text-5xl font-black leading-none tracking-tight text-primary md:text-7xl">Built for repeat hydration, not a one-time trick.</h2>
            <p className="mt-8 max-w-2xl text-xl font-semibold leading-8 text-on-surface-variant">
              Use strong product proof, simple science, and fast paths to purchase. Keep ecommerce pages separate; let the home page sell the promise.
            </p>
          </div>
          <div className="grid gap-4">
            {proofPoints.map((point) => (
              <div key={point.detail} className="rounded-lg bg-primary p-8 text-white">
                <p className="text-6xl font-black tracking-tight">{point.label}</p>
                <p className="mt-2 text-lg font-black uppercase tracking-[0.2em] text-secondary">{point.detail}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}
