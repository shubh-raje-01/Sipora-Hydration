import React from 'react';
import { motion } from 'motion/react';
import { HelpCircle, Truck, Droplets, Zap, Plus, Mail, MapPin, Send } from 'lucide-react';

export default function SupportPage() {
  return (
    <div className="flex flex-col w-full">
      <section className="relative px-8 pt-32 pb-24 overflow-hidden">
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          <div className="lg:col-span-7 z-10">
            <motion.h1 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="text-7xl font-bold tracking-tighter leading-none text-on-surface mb-8"
            >
              Flow with <span className="text-primary italic">Precision.</span>
            </motion.h1>
            <p className="text-xl text-on-surface-variant max-w-xl leading-relaxed">
              Whether it's a technical query or a quest for the perfect hydration strategy, the Sipora support ecosystem is designed to move at your speed.
            </p>
          </div>
          <div className="lg:col-span-5 relative">
            <div className="aspect-square rounded-full kinetic-gradient opacity-10 blur-3xl absolute -top-10 -right-10 w-full h-full"></div>
            <div className="relative bg-white p-10 rounded-[3rem] shadow-2xl border border-white/20">
              <div className="flex flex-col gap-8">
                <div className="flex items-center gap-6">
                  <div className="w-14 h-14 rounded-full kinetic-gradient flex items-center justify-center text-white shadow-lg">
                    <HelpCircle className="w-7 h-7" />
                  </div>
                  <div>
                    <h3 className="font-bold text-xl">24/7 Digital Concierge</h3>
                    <p className="text-on-surface-variant text-sm">Response time: &lt; 5 mins</p>
                  </div>
                </div>
                <div className="flex items-center gap-6">
                  <div className="w-14 h-14 rounded-full bg-secondary-container flex items-center justify-center text-on-surface shadow-lg">
                    <Truck className="w-7 h-7" />
                  </div>
                  <div>
                    <h3 className="font-bold text-xl">Real-time Logistics</h3>
                    <p className="text-on-surface-variant text-sm">Track your hydration gear</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="px-8 py-32 bg-surface-container-low">
        <div className="max-w-7xl mx-auto">
          <div className="mb-16">
            <span className="text-primary font-bold tracking-widest text-sm uppercase">Legacy & Innovation</span>
            <h2 className="text-5xl font-bold mt-4">The Sipora Origin</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="md:col-span-2 bg-white p-16 rounded-[3rem] flex flex-col justify-end min-h-[450px] relative overflow-hidden group shadow-sm">
              <img 
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuAinijup4PlR8zsxz2WN2Rg4Io0ipD7yttRlTSYaeDoNlnyoA5xVtWF9RXlAI9A8eSttAIKkFkNk2ud9IkS1xCCe8guZLuLyryNU1A6eqkLcnVjz0J4eZYB-nB_D_AiwbRaLwmsYg4m5grNCbXo-O42akdbMYaID0M4CIlkqkk_qcN815h2DnXtcEWQfG1OHnDxzA1QZ7Ej2gvn3sc-Ah3rC9_BKzgnf0u90P7qggPVubu5YJC_wn1ZjdJlBBDPJVjrzCVk2x7dsg" 
                alt="Swiss Alps"
                className="absolute inset-0 w-full h-full object-cover opacity-20 group-hover:scale-105 transition-transform duration-1000"
                referrerPolicy="no-referrer"
              />
              <div className="relative z-10">
                <h3 className="text-4xl font-bold mb-6">Born from Necessity</h3>
                <p className="text-on-surface-variant text-xl max-w-lg leading-relaxed">
                  Founded in the high-altitude labs of the Swiss Alps, Sipora was created to solve one problem: adaptive hydration. We don't just make bottles; we engineer vitality.
                </p>
              </div>
            </div>
            <div className="bg-primary p-12 rounded-[3rem] flex flex-col justify-between text-white shadow-xl">
              <Droplets className="w-12 h-12" />
              <div>
                <h3 className="text-3xl font-bold mb-3">99.9% Purity</h3>
                <p className="text-white/80 text-lg">Every Sipora vessel features our proprietary ionic filtration tech.</p>
              </div>
            </div>
            <div className="bg-secondary p-12 rounded-[3rem] flex flex-col justify-between text-white shadow-xl">
              <Zap className="w-12 h-12" />
              <div>
                <h3 className="text-3xl font-bold mb-3">Flow Control</h3>
                <p className="text-white/80 text-lg">Dynamic valves that adjust to your physical exertion levels.</p>
              </div>
            </div>
            <div className="md:col-span-2 bg-white p-16 rounded-[3rem] flex items-center gap-16 shadow-sm">
              <div className="hidden lg:block w-56 h-56 rounded-full overflow-hidden shrink-0 shadow-2xl">
                <img 
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuBnhG__9id_yzNBXHGNNm1ZsaJHdsVfFjKgQARQnLFhe-5Q309LriU1MUy46a6uZaNVNR6JU0Idm7KWYD0HXw5PeADQdNFDaGUKFkbo_U-e-z8imSd5WktUYkcfxEQy4cxSR5Tlh8cjMEY8vAH-UjgUtK5AFnNguQA3eXNc__f0Xz9QJjiM0t1bsSl2d6Y5t0y8AoUcbFLRsEduU-aEkKFAo4KvWirEKihelL2Qb7aUKJmT5hf7FbUgGngmuevIjKi43UM7tc1Z5Q" 
                  alt="Team"
                  className="w-full h-full object-cover"
                  referrerPolicy="no-referrer"
                />
              </div>
              <div>
                <h3 className="text-4xl font-bold mb-6">The Collective</h3>
                <p className="text-on-surface-variant text-lg leading-relaxed">
                  Our team consists of biophysicists, ultra-marathoners, and industrial designers. Together, we are redefining what it means to be truly hydrated in a fast-moving world.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="px-8 py-32">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl font-bold mb-6">Common Currents</h2>
            <p className="text-xl text-on-surface-variant">Frequently asked questions about Sipora technology and care.</p>
          </div>
          <div className="space-y-6">
            {[
              { q: "How does the adaptive flow valve work?", a: "The Sipora valve uses pressure-sensitive membranes that react to the suction force and ambient temperature. As you increase intensity, the valve expands to provide a higher flow rate without spilling." },
              { q: "Is the Sipora filtration system replaceable?", a: "Yes, the SIP-7 filter core should be replaced every 3 months or 150 liters of water. Replacement packs are available in our shop section." },
              { q: "What is the warranty period for Sipora hardware?", a: "Every Sipora product comes with a standard 2-year warranty covering manufacturing defects. We also offer a lifetime 'Flow Guarantee' on all mechanical valves." },
              { q: "How do I clean my Sipora vessel?", a: "While Sipora is dishwasher safe (top rack), we recommend hand washing with warm soapy water to maintain the integrity of the thermal coating." }
            ].map((item, i) => (
              <div key={i} className="bg-surface-container-low rounded-[2rem] overflow-hidden">
                <button className="w-full px-10 py-8 text-left flex justify-between items-center hover:bg-surface-container transition-colors group">
                  <span className="font-bold text-2xl">{item.q}</span>
                  <Plus className="w-6 h-6 text-primary group-hover:rotate-45 transition-transform" />
                </button>
                <div className="px-10 pb-8 text-on-surface-variant text-lg leading-relaxed">
                  {item.a}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="px-8 py-32 mb-20">
        <div className="max-w-7xl mx-auto bg-on-surface rounded-[4rem] p-16 lg:p-24 overflow-hidden relative shadow-2xl">
          <div className="absolute top-0 right-0 w-1/3 h-full kinetic-gradient opacity-10 blur-[100px] -rotate-12 translate-x-1/2"></div>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-24 relative z-10">
            <div>
              <h2 className="text-6xl font-bold text-white mb-10 leading-tight">Need a direct <span className="text-primary italic">Connection?</span></h2>
              <p className="text-stone-400 text-xl mb-16 leading-relaxed">Drop our team a message. We specialize in rapid resolution and technical guidance for high-performance hydration.</p>
              <div className="space-y-10">
                <div className="flex items-center gap-8">
                  <div className="w-16 h-16 rounded-2xl bg-white/5 flex items-center justify-center text-primary border border-white/10 shadow-xl">
                    <Mail className="w-8 h-8" />
                  </div>
                  <div>
                    <h4 className="text-white font-bold text-xl">Email us</h4>
                    <p className="text-stone-500 text-lg">support@sipora.tech</p>
                  </div>
                </div>
                <div className="flex items-center gap-8">
                  <div className="w-16 h-16 rounded-2xl bg-white/5 flex items-center justify-center text-primary border border-white/10 shadow-xl">
                    <MapPin className="w-8 h-8" />
                  </div>
                  <div>
                    <h4 className="text-white font-bold text-xl">Global HQ</h4>
                    <p className="text-stone-500 text-lg">Zurich, Switzerland</p>
                  </div>
                </div>
              </div>
            </div>
            <form className="space-y-8" onSubmit={(e) => e.preventDefault()}>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="space-y-3">
                  <label className="text-stone-400 text-sm font-bold ml-1 uppercase tracking-widest">Your Name</label>
                  <input type="text" placeholder="John Doe" className="w-full bg-white/5 border-none rounded-2xl px-8 py-5 text-white focus:ring-2 focus:ring-primary/40 transition-all placeholder:text-stone-700 text-lg" />
                </div>
                <div className="space-y-3">
                  <label className="text-stone-400 text-sm font-bold ml-1 uppercase tracking-widest">Email Address</label>
                  <input type="email" placeholder="john@example.com" className="w-full bg-white/5 border-none rounded-2xl px-8 py-5 text-white focus:ring-2 focus:ring-primary/40 transition-all placeholder:text-stone-700 text-lg" />
                </div>
              </div>
              <div className="space-y-3">
                <label className="text-stone-400 text-sm font-bold ml-1 uppercase tracking-widest">Topic</label>
                <select className="w-full bg-white/5 border-none rounded-2xl px-8 py-5 text-white focus:ring-2 focus:ring-primary/40 transition-all appearance-none text-lg">
                  <option className="bg-stone-900">Technical Support</option>
                  <option className="bg-stone-900">Order Inquiry</option>
                  <option className="bg-stone-900">Partnership</option>
                  <option className="bg-stone-900">Other</option>
                </select>
              </div>
              <div className="space-y-3">
                <label className="text-stone-400 text-sm font-bold ml-1 uppercase tracking-widest">How can we help?</label>
                <textarea placeholder="Tell us more about your inquiry..." rows={5} className="w-full bg-white/5 border-none rounded-2xl px-8 py-5 text-white focus:ring-2 focus:ring-primary/40 transition-all placeholder:text-stone-700 text-lg"></textarea>
              </div>
              <button className="w-full kinetic-gradient text-white py-6 rounded-[1.5rem] font-bold text-xl hover:shadow-2xl hover:shadow-primary/20 transition-all active:scale-[0.98] flex items-center justify-center gap-3">
                Send Message
                <Send className="w-5 h-5" />
              </button>
            </form>
          </div>
        </div>
      </section>
    </div>
  );
}
