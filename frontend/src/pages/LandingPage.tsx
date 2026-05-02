import React from 'react';
import { motion, useScroll, useTransform } from 'motion/react';
import { ArrowRight, Check, Sparkles } from 'lucide-react';
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

function AirUpStyleBottleStage() {
  const [tilt, setTilt] = React.useState({ x: 0, y: 0 });
  const { scrollYProgress } = useScroll();
  const rotateY = useTransform(scrollYProgress, [0, 0.32], [-18, 26]);
  const rotateZ = useTransform(scrollYProgress, [0, 0.32], [-4, 5]);
  const lift = useTransform(scrollYProgress, [0, 0.32], [20, -54]);
  const sideSpread = useTransform(scrollYProgress, [0, 0.32], [42, 112]);
  const leftBottleX = useTransform(sideSpread, (value) => value * -1);
  const rightBottleX = useTransform(sideSpread, (value) => value);
  const podOrbit = useTransform(scrollYProgress, [0, 0.32], [-28, 46]);

  return (
    <motion.div
      className="relative min-h-[38rem] overflow-hidden rounded-lg bg-white/35 p-5 shadow-2xl shadow-primary/10 backdrop-blur-md"
      onMouseMove={(event) => {
        const rect = event.currentTarget.getBoundingClientRect();
        setTilt({
          x: ((event.clientX - rect.left) / rect.width - 0.5) * 18,
          y: ((event.clientY - rect.top) / rect.height - 0.5) * -14,
        });
      }}
      onMouseLeave={() => setTilt({ x: 0, y: 0 })}
    >
      <div className="absolute left-6 top-6 z-20 rounded-full bg-primary px-4 py-2 text-xs font-black uppercase tracking-[0.2em] text-white">
        New colors
      </div>
      <div className="absolute bottom-6 left-6 z-20 max-w-[12rem] text-sm font-black uppercase tracking-[0.18em] text-primary/80">
        Scroll the bottle
      </div>
      <motion.div className="absolute inset-x-8 top-24 h-64 rounded-[50%] bg-secondary/40 blur-3xl" style={{ x: sideSpread }} />

      <div className="absolute inset-0 flex items-center justify-center [perspective:1400px]">
        {[
          { color: 'from-tertiary-container to-white', x: leftBottleX, rotate: -8 },
          { color: 'from-secondary to-white', x: rightBottleX, rotate: 8 },
        ].map((ghost) => (
          <motion.div
            key={ghost.rotate}
            className={`absolute h-[29rem] w-[10rem] rounded-[4rem] border border-white/70 bg-gradient-to-br ${ghost.color} shadow-xl`}
            style={{ x: ghost.x, scale: 0.82, opacity: 0.48, rotateZ: ghost.rotate }}
            aria-hidden="true"
          />
        ))}

        <motion.div
          className="relative h-[34rem] w-[14rem] [transform-style:preserve-3d]"
          style={{
            y: lift,
            rotateY,
            rotateZ,
            rotateX: tilt.y,
            x: tilt.x,
            transformPerspective: 1200,
          }}
        >
          <motion.div
            className="absolute left-1/2 top-0 h-20 w-24 -translate-x-1/2 rounded-t-[2.25rem] rounded-b-xl bg-primary shadow-2xl"
            animate={{ y: [0, -8, 0] }}
            transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}
          />
          <div className="absolute left-1/2 top-14 h-[29rem] w-[12rem] -translate-x-1/2 overflow-hidden rounded-[4.5rem] border border-white/80 bg-gradient-to-br from-white via-secondary/90 to-tertiary-container shadow-[0_48px_100px_rgba(15,40,47,0.26)]">
            <div className="absolute left-7 top-10 h-80 w-7 rounded-full bg-white/70 blur-sm" />
            <div className="absolute right-7 top-20 h-60 w-5 rounded-full bg-primary/10 blur-sm" />
            <div className="absolute left-1/2 top-36 flex h-36 w-16 -translate-x-1/2 items-center justify-center rounded-full bg-primary text-[0.65rem] font-black uppercase tracking-[0.24em] text-white [writing-mode:vertical-rl]">
              Sipora
            </div>
            <div className="absolute bottom-10 left-1/2 h-20 w-20 -translate-x-1/2 rounded-full border-8 border-white/70 bg-secondary shadow-inner" />
          </div>
          <motion.div
            className="absolute left-[68%] top-28 h-20 w-20 rounded-full border-[0.55rem] border-white/80 bg-tertiary shadow-2xl"
            style={{ rotate: podOrbit }}
          />
        </motion.div>
      </div>
    </motion.div>
  );
}

function ProductTheaterStage() {
  const [tilt, setTilt] = React.useState({ x: 0, y: 0 });
  const { scrollYProgress } = useScroll();
  const y = useTransform(scrollYProgress, [0, 0.25], [0, -58]);
  const rotate = useTransform(scrollYProgress, [0, 0.25], [-5, 4]);
  const scale = useTransform(scrollYProgress, [0, 0.25], [1, 0.94]);
  const podLeft = useTransform(scrollYProgress, [0, 0.25], [-52, -112]);
  const podRight = useTransform(scrollYProgress, [0, 0.25], [52, 122]);

  return (
    <motion.div
      className="relative min-h-[36rem] overflow-hidden rounded-lg bg-white/64 shadow-2xl shadow-primary/10 backdrop-blur-md"
      onMouseMove={(event) => {
        const rect = event.currentTarget.getBoundingClientRect();
        setTilt({
          x: ((event.clientX - rect.left) / rect.width - 0.5) * 16,
          y: ((event.clientY - rect.top) / rect.height - 0.5) * -12,
        });
      }}
      onMouseLeave={() => setTilt({ x: 0, y: 0 })}
    >
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_40%,rgba(144,214,249,0.42),transparent_22rem),radial-gradient(circle_at_70%_70%,rgba(255,188,217,0.36),transparent_18rem)]" />
      <div className="absolute left-6 top-6 z-20 rounded-full bg-secondary px-4 py-2 text-xs font-black uppercase tracking-[0.2em] text-primary">
        Starter kit
      </div>
      <div className="absolute bottom-6 left-6 right-6 z-20 grid grid-cols-3 gap-2 text-center text-[0.68rem] font-black uppercase tracking-[0.16em] text-primary/70">
        <span className="rounded-full bg-white/80 px-2 py-2">750 ml</span>
        <span className="rounded-full bg-white/80 px-2 py-2">0g sugar</span>
        <span className="rounded-full bg-white/80 px-2 py-2">3 pods</span>
      </div>

      <div className="absolute inset-0 flex items-center justify-center [perspective:1400px]">
        <motion.div
          className="absolute top-[46%] h-24 w-24 rounded-full border-[0.55rem] border-white/80 bg-tertiary shadow-xl"
          style={{ x: podLeft, rotate: -16 }}
        />
        <motion.div
          className="absolute top-[35%] h-20 w-20 rounded-full border-[0.5rem] border-white/80 bg-secondary shadow-xl"
          style={{ x: podRight, rotate: 22 }}
        />

        <motion.div
          className="relative h-[32rem] w-[13rem] [transform-style:preserve-3d]"
          style={{
            y,
            rotate,
            scale,
            x: tilt.x,
            rotateX: tilt.y,
            transformPerspective: 1200,
          }}
        >
          <motion.div
            className="absolute left-1/2 top-0 h-20 w-24 -translate-x-1/2 rounded-t-[2.25rem] rounded-b-xl bg-primary shadow-2xl"
            animate={{ y: [0, -7, 0] }}
            transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}
          />
          <div className="absolute left-1/2 top-14 h-[27rem] w-[11.5rem] -translate-x-1/2 overflow-hidden rounded-[4.4rem] border border-white/90 bg-gradient-to-br from-white via-secondary/90 to-tertiary-container shadow-[0_42px_90px_rgba(15,40,47,0.24)]">
            <div className="absolute left-7 top-10 h-80 w-7 rounded-full bg-white/72 blur-sm" />
            <div className="absolute right-7 top-20 h-60 w-5 rounded-full bg-primary/10 blur-sm" />
            <div className="absolute left-1/2 top-32 flex h-36 w-16 -translate-x-1/2 items-center justify-center rounded-full bg-primary text-[0.65rem] font-black uppercase tracking-[0.24em] text-white [writing-mode:vertical-rl]">
              Sipora
            </div>
            <div className="absolute bottom-10 left-1/2 h-20 w-20 -translate-x-1/2 rounded-full border-8 border-white/70 bg-secondary shadow-inner" />
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
}

function MagicProductCard({ product, index }: { product: Product; index: number }) {
  const [spot, setSpot] = React.useState({ x: '50%', y: '50%' });
  const isBottle = product.category === 'vessel';

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
      whileHover={{ y: -12, rotateX: isBottle ? 4 : 2, rotateY: isBottle ? -5 : 5 }}
      className="group relative overflow-hidden rounded-lg border border-white/70 bg-white/78 p-5 shadow-xl shadow-primary/10 [transform-style:preserve-3d]"
    >
      <div className="card-shine pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-500 group-hover:opacity-100" style={{ '--x': spot.x, '--y': spot.y } as React.CSSProperties} />
      <span className="absolute right-4 top-4 z-20 rounded-full bg-primary/90 px-3 py-1 text-[0.63rem] font-black uppercase tracking-[0.18em] text-white opacity-0 transition duration-300 group-hover:opacity-100">
        {isBottle ? 'Bottle' : 'Pod'}
      </span>
      <div className="relative aspect-[4/5] overflow-hidden rounded-lg bg-secondary-container">
        <motion.div
          className={`absolute inset-x-8 bottom-8 z-10 h-20 rounded-full blur-2xl ${isBottle ? 'bg-secondary/60' : 'bg-tertiary/60'}`}
          animate={{ scale: [0.94, 1.08, 0.94], opacity: [0.32, 0.72, 0.32] }}
          transition={{ duration: isBottle ? 3.8 : 2.8, repeat: Infinity, ease: 'easeInOut' }}
        />
        <img
          src={product.image}
          alt={product.name}
          className={`relative z-10 h-full w-full object-cover transition duration-700 ${isBottle ? 'group-hover:scale-110 group-hover:-rotate-3' : 'group-hover:scale-[1.15] group-hover:rotate-6'}`}
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

function FadeInSection({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return (
    <motion.section
      initial={{ opacity: 0, y: 44 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-120px' }}
      transition={{ duration: 0.7, ease: 'easeOut' }}
      className={className}
    >
      {children}
    </motion.section>
  );
}

function StickyBuyButton() {
  const { scrollYProgress } = useScroll();
  const opacity = useTransform(scrollYProgress, [0, 0.12, 0.88, 1], [0, 1, 1, 0]);
  const y = useTransform(scrollYProgress, [0, 0.12], [30, 0]);

  return (
    <motion.div style={{ opacity, y }} className="fixed bottom-5 right-5 z-40">
      <Link to="/shop" className="kinetic-gradient flex items-center gap-3 rounded-full px-6 py-4 text-sm font-black text-white shadow-2xl shadow-primary/25">
        Buy now <ArrowRight className="h-4 w-4" />
      </Link>
    </motion.div>
  );
}

export default function LandingPage() {
  const featured = [...VESSELS.slice(0, 2), ...PODS.slice(0, 2)];
  const productColors = ['#0F282F', '#90D6F9', '#FFBCD9', '#F3F6F6'];
  const theaterProof = ['No sugar', 'Scent-based flavor', 'Reusable bottle'];
  const heroRef = React.useRef<HTMLElement | null>(null);
  const { scrollYProgress: heroProgress } = useScroll({
    target: heroRef,
    offset: ['start start', 'end start'],
  });
  const waveY = useTransform(heroProgress, [0, 1], [40, -90]);
  const waveX = useTransform(heroProgress, [0, 1], [-48, 80]);
  const heroContentY = useTransform(heroProgress, [0, 1], [0, -70]);
  const heroOpacity = useTransform(heroProgress, [0, 0.78, 1], [1, 0.85, 0]);
  const heroGridY = useTransform(heroProgress, [0, 1], [0, -130]);

  return (
    <div className="w-full overflow-hidden">
      <StickyBuyButton />
      <section ref={heroRef} className="relative min-h-screen bg-background px-5 pt-28 md:px-8">
        <motion.div className="hero-wave absolute inset-x-[-12%] bottom-10 h-56 opacity-35 [animation:wave-slide_16s_ease-in-out_infinite_alternate]" style={{ x: waveX, y: waveY }} />
        <div className="absolute inset-0 z-0 bg-[linear-gradient(90deg,rgba(243,246,246,0.88)_0%,rgba(243,246,246,0.68)_48%,rgba(243,246,246,0.22)_100%)]" />
        <div className="relative z-10 mx-auto grid max-w-7xl grid-cols-1 items-center gap-10 pb-16 lg:min-h-[calc(100vh-7rem)] lg:grid-cols-[0.92fr_1.08fr]">
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            style={{ y: heroContentY, opacity: heroOpacity }}
            transition={{ duration: 0.7 }}
            className="relative z-10"
          >
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-primary/10 bg-white/80 px-4 py-2 text-xs font-black uppercase tracking-[0.2em] text-primary">
              <Sparkles className="h-4 w-4 text-tertiary" />
              Sipora starter kit
            </div>
            <h1 className="max-w-4xl text-6xl font-black leading-[0.88] tracking-tight text-primary md:text-8xl">
              Water, but it finally has a mood.
            </h1>
            <p className="mt-8 max-w-xl text-lg font-semibold leading-8 text-on-surface-variant md:text-xl">
              A reusable bottle and aroma pods that make plain water feel flavored, without adding sugar or syrup.
            </p>
            <div className="mt-8 flex items-end gap-5">
              <div>
                <p className="text-xs font-black uppercase tracking-[0.2em] text-on-surface-variant">From</p>
                <p className="text-4xl font-black text-primary">$49</p>
              </div>
              <div className="flex gap-2 pb-2">
                {productColors.map((color) => (
                  <span key={color} className="h-8 w-8 rounded-full border-2 border-white shadow-md" style={{ backgroundColor: color }} />
                ))}
              </div>
            </div>
            <div className="mt-8 flex flex-wrap gap-4">
              <Link to="/shop" className="kinetic-gradient inline-flex items-center gap-3 rounded-full px-8 py-4 text-base font-black text-white shadow-xl shadow-primary/20 transition hover:-translate-y-1">
                Buy starter kit <ArrowRight className="h-5 w-5" />
              </Link>
              <a href="#how-it-works" className="inline-flex items-center gap-3 rounded-full bg-white px-8 py-4 text-base font-black text-primary shadow-lg shadow-primary/10 transition hover:-translate-y-1">
                How it works
              </a>
            </div>
            <div className="mt-8 grid max-w-xl gap-3 sm:grid-cols-3">
              {theaterProof.map((item) => (
                <div key={item} className="flex items-center gap-2 rounded-lg bg-white/70 px-3 py-3 text-sm font-black text-primary shadow-sm">
                  <Check className="h-4 w-4 text-tertiary" />
                  {item}
                </div>
              ))}
            </div>
          </motion.div>

          <motion.div style={{ y: heroGridY }}>
            <ProductTheaterStage />
          </motion.div>
        </div>
      </section>

      <FadeInSection className="bg-secondary py-5 text-primary">
        <div className="flex min-w-max animate-[marquee_24s_linear_infinite] gap-12 text-2xl font-black uppercase tracking-[0.18em]">
          {Array.from({ length: 8 }).map((_, index) => (
            <span key={index}>Water first / scent second / sugar never</span>
          ))}
        </div>
      </FadeInSection>

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
                <div className="mb-10 flex items-center gap-4">
                  <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white/80 text-sm font-black text-primary shadow-lg">{index + 1}</span>
                  <p className="text-sm font-black uppercase tracking-[0.26em] opacity-70">{card.kicker}</p>
                </div>
                <h3 className="mt-8 max-w-2xl text-5xl font-black leading-none tracking-tight md:text-7xl">{card.title}</h3>
                <p className="mt-8 max-w-xl text-xl font-semibold leading-8 opacity-80">{card.body}</p>
              </motion.article>
            ))}
          </div>
        </div>
      </section>

      <FadeInSection className="bg-white px-5 py-28 md:px-8">
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
      </FadeInSection>

      <FadeInSection className="px-5 py-28 md:px-8">
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
      </FadeInSection>
    </div>
  );
}
