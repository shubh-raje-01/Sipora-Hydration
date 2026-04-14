import React from 'react';
import { motion } from 'motion/react';
import { ArrowRight, Play, Brain, ShieldCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import { PODS } from '../constants';

export default function LandingPage() {
  return (
    <div className="flex flex-col w-full">
      {/* Hero Section */}
      <section className="relative min-h-[90vh] flex items-center px-8 overflow-hidden pt-20">
        <div className="max-w-7xl mx-auto w-full grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <motion.div 
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8, ease: "easeOut" }}
            className="z-10"
          >
            <span className="inline-block px-4 py-1 rounded-full bg-secondary-container text-on-surface font-bold text-xs tracking-widest mb-6 uppercase">
              The Future of Hydration
            </span>
            <h1 className="text-7xl md:text-8xl font-bold leading-[0.9] tracking-tighter mb-8 text-primary">
              Sip the <br/><span className="text-on-surface">Scent.</span>
            </h1>
            <p className="text-xl md:text-2xl text-on-surface-variant max-w-lg mb-10 leading-relaxed text-balance">
              Transform pure water into a flavor explosion using only the power of aroma. No sugar. No chemicals. Just brain-tricked bliss.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link to="/shop" className="kinetic-gradient text-white px-10 py-5 rounded-full text-lg font-extrabold flex items-center gap-3 shadow-xl hover:scale-105 transition-transform">
                Explore Flavor Pods
                <ArrowRight className="w-6 h-6" />
              </Link>
              <button className="bg-surface-container-highest text-primary px-10 py-5 rounded-full text-lg font-bold hover:bg-surface-container-high transition-colors">
                How it Works
              </button>
            </div>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, scale: 0.8, rotate: 10 }}
            animate={{ opacity: 1, scale: 1, rotate: 3 }}
            transition={{ duration: 1, ease: "easeOut" }}
            className="relative"
          >
            <div className="absolute -top-20 -right-20 w-96 h-96 bg-tertiary-container/20 rounded-full blur-[100px]"></div>
            <div className="relative z-10 aspect-square rounded-[3rem] overflow-hidden shadow-2xl hover:rotate-0 transition-transform duration-700">
              <img 
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuAxy0LeM0m_TkFAVs3U3EvtOA5yv-EiAjLGX5qedkS6nEhuPUDtrfL3IvyjgoZE8OX4Bmkgg9rtbiCtTYE2J6T-dlMgPEfeHkRww8ch3kHymRWboaO5Nbnk2JYUhlBkYve6OUeDtSZCAPbljHBTeYniZV87R0QwEJA1oxfY1CrWt9ShvMk9utt3FnrTK0uKZWmsCkI7b1IdnGgOiRsFFSm4ZrFhY8au009v5idILBperBCFuQslth21JAZ5MOTKzsyUgp6L2mgaZA" 
                alt="Sipora bottle"
                className="w-full h-full object-cover"
                referrerPolicy="no-referrer"
              />
            </div>
          </motion.div>
        </div>
      </section>

      {/* Technology Section */}
      <section id="tech" className="py-32 bg-surface-container-low">
        <div className="max-w-7xl mx-auto px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-16 items-center">
            <div className="md:col-span-2">
              <h2 className="text-5xl md:text-6xl font-bold tracking-tight mb-8 leading-tight">
                Retronasal Olfaction: <br/>The Science of Flavor
              </h2>
              <div className="space-y-6 text-xl text-on-surface-variant leading-relaxed">
                <p>Most of what we perceive as "taste" is actually smell. When you sip through a Sipora bottle, scented air travels from the pod through your mouth to your olfactory receptors.</p>
                <p>Your brain interprets these scent signals as taste, creating a rich, flavorful experience without a single gram of sugar or artificial additives. It's the ultimate hydration hack.</p>
              </div>
            </div>
            <motion.div 
              whileHover={{ y: -10 }}
              className="bg-surface-container-high p-10 rounded-[2.5rem] flex flex-col justify-center border-l-8 border-primary shadow-lg"
            >
              <Brain className="w-16 h-16 text-primary mb-6" />
              <h3 className="text-3xl font-bold mb-4">100% Scent</h3>
              <p className="text-on-surface-variant text-lg">Your brain does the work. Your body reaps the hydration.</p>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Video Section */}
      <section className="py-32 px-8">
        <div className="max-w-6xl mx-auto">
          <motion.div 
            initial={{ opacity: 0, y: 50 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="relative rounded-[3rem] overflow-hidden aspect-video shadow-2xl group cursor-pointer bg-stone-900"
          >
            <img 
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuCo4yWdBMzp1G_MYXGv8grmK0NhSs4IY3ywFzyZbI4lJ9b0ozYKRGVvmBoXE2wCLvUfHSbjuq6C0cNN7afR-xi_BdO0nucNGGUn4QGHMg56M5cFoHs9G4rVRKPmSV895aZ8bopONWj-y_FzUKxtCa7--tMRzGaUYeZvb6q2b9KfX8_e_BaSKOuJqEOf6_9wrce3shUL3n2yKysmutM1txUjRHSzpKt81NJR-Cg4YC16MIeOLIlNA0pICqr9W_chJ-1pvaX5z44O0g" 
              alt="Lifestyle video"
              className="w-full h-full object-cover opacity-60 group-hover:scale-105 transition-transform duration-1000"
              referrerPolicy="no-referrer"
            />
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="w-24 h-24 bg-primary rounded-full flex items-center justify-center text-white shadow-2xl group-hover:scale-110 transition-transform">
                <Play className="w-10 h-10 fill-current" />
              </div>
            </div>
            <div className="absolute bottom-12 left-12 text-white">
              <h3 className="text-3xl font-bold mb-2">Watch: The Sipora Experience</h3>
              <p className="text-lg opacity-80">See how we're changing the way the world drinks water.</p>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Popular Picks */}
      <section className="py-32 bg-surface">
        <div className="max-w-7xl mx-auto px-8">
          <div className="flex justify-between items-end mb-16">
            <div>
              <h2 className="text-5xl font-bold tracking-tight mb-4">Popular Picks</h2>
              <p className="text-xl text-on-surface-variant">The flavors everyone is obsessed with.</p>
            </div>
            <Link to="/shop" className="text-primary font-bold flex items-center gap-2 group">
              View All Flavors
              <ArrowRight className="w-5 h-5 transition-transform group-hover:translate-x-1" />
            </Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {PODS.map((pod) => (
              <motion.div 
                key={pod.id}
                whileHover={{ y: -10 }}
                className="group"
              >
                <div className="aspect-[4/5] rounded-3xl overflow-hidden bg-surface-container mb-6 relative">
                  <img 
                    src={pod.image} 
                    alt={pod.name}
                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                    referrerPolicy="no-referrer"
                  />
                  {pod.tag && (
                    <div className="absolute top-4 right-4 bg-white/90 backdrop-blur px-3 py-1 rounded-full text-[10px] font-bold text-primary uppercase tracking-widest">
                      {pod.tag}
                    </div>
                  )}
                </div>
                <h3 className="text-xl font-bold mb-1">{pod.name}</h3>
                <p className="text-on-surface-variant mb-4 font-medium">3-Pod Pack • ${pod.price.toFixed(2)}</p>
                <button className="w-full py-4 bg-surface-container-highest text-on-surface font-bold rounded-2xl group-hover:bg-primary group-hover:text-white transition-all active:scale-95">
                  Add to Cart
                </button>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Newsletter */}
      <section className="py-32">
        <div className="max-w-7xl mx-auto px-8">
          <div className="kinetic-gradient rounded-[4rem] p-16 md:p-24 text-center relative overflow-hidden shadow-2xl shadow-primary/40">
            <div className="absolute top-0 left-0 w-full h-full opacity-10 pointer-events-none">
              <div className="absolute top-10 left-10 w-64 h-64 border-4 border-white rounded-full"></div>
              <div className="absolute bottom-[-100px] right-20 w-96 h-96 border-4 border-white rounded-full"></div>
            </div>
            <div className="relative z-10">
              <h2 className="text-5xl md:text-7xl font-bold text-white mb-8 tracking-tighter">Join the Flavor Revolution</h2>
              <p className="text-xl md:text-2xl text-white/80 max-w-2xl mx-auto mb-12">Be the first to know about new pods, limited editions, and the future of scent-based taste.</p>
              <form className="max-w-md mx-auto flex flex-col sm:flex-row gap-4" onSubmit={(e) => e.preventDefault()}>
                <input 
                  type="email" 
                  placeholder="Enter your email" 
                  className="flex-1 px-8 py-5 rounded-full border-none focus:ring-4 focus:ring-white/20 text-lg placeholder:text-stone-400 bg-white shadow-inner"
                />
                <button className="bg-on-surface text-white px-10 py-5 rounded-full font-extrabold text-lg hover:bg-stone-800 transition-colors shadow-lg">
                  Subscribe
                </button>
              </form>
              <p className="mt-8 text-white/60 text-sm">By subscribing, you agree to our Privacy Policy. No spam, ever.</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
