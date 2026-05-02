import React from 'react';
import { Link } from 'react-router-dom';
import { Globe, Mail, Share2 } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="w-full bg-primary px-8 py-16 text-white">
      <div className="mx-auto grid max-w-7xl grid-cols-1 gap-12 md:grid-cols-4">
        <div className="space-y-6">
          <Link to="/" className="font-headline text-xl font-black text-secondary">Sipora</Link>
          <p className="max-w-xs text-sm leading-relaxed text-white/70">
            Revolutionizing hydration through the science of retronasal olfaction. Drink different.
          </p>
          <div className="flex gap-4">
            <a href="#" className="text-secondary transition-colors hover:text-tertiary"><Globe className="h-5 w-5" /></a>
            <a href="#" className="text-secondary transition-colors hover:text-tertiary"><Mail className="h-5 w-5" /></a>
            <a href="#" className="text-secondary transition-colors hover:text-tertiary"><Share2 className="h-5 w-5" /></a>
          </div>
        </div>

        <div>
          <h4 className="mb-6 text-xs font-bold uppercase tracking-widest text-secondary">Shop</h4>
          <ul className="space-y-4 text-sm font-medium text-white/70">
            <li><Link to="/shop" className="transition-colors hover:text-secondary">Water Bottles</Link></li>
            <li><Link to="/shop" className="transition-colors hover:text-secondary">Scent Pods</Link></li>
            <li><a href="#" className="transition-colors hover:text-secondary">Accessories</a></li>
            <li><a href="#" className="transition-colors hover:text-secondary">Gift Cards</a></li>
          </ul>
        </div>

        <div>
          <h4 className="mb-6 text-xs font-bold uppercase tracking-widest text-secondary">Company</h4>
          <ul className="space-y-4 text-sm font-medium text-white/70">
            <li><a href="#" className="transition-colors hover:text-secondary">Our Story</a></li>
            <li><a href="#" className="transition-colors hover:text-secondary">Technology</a></li>
            <li><a href="#" className="transition-colors hover:text-secondary">Sustainability</a></li>
            <li><a href="#" className="transition-colors hover:text-secondary">Careers</a></li>
          </ul>
        </div>

        <div>
          <h4 className="mb-6 text-xs font-bold uppercase tracking-widest text-secondary">Support</h4>
          <ul className="space-y-4 text-sm font-medium text-white/70">
            <li><Link to="/support" className="transition-colors hover:text-secondary">Help Center</Link></li>
            <li><a href="#" className="transition-colors hover:text-secondary">Shipping</a></li>
            <li><a href="#" className="transition-colors hover:text-secondary">Returns</a></li>
            <li><Link to="/support" className="transition-colors hover:text-secondary">Contact</Link></li>
          </ul>
        </div>
      </div>

      <div className="mx-auto mt-20 flex max-w-7xl flex-col items-center justify-between gap-6 border-t border-white/10 pt-8 text-sm text-white/60 md:flex-row">
        <p>&copy; 2026 Sipora. All rights reserved.</p>
        <div className="flex gap-8 font-medium">
          <a href="#" className="transition-colors hover:text-secondary">Privacy Policy</a>
          <a href="#" className="transition-colors hover:text-secondary">Terms of Service</a>
          <a href="#" className="transition-colors hover:text-secondary">Cookies</a>
        </div>
      </div>
    </footer>
  );
}
