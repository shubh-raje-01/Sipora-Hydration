import React from 'react';
import { Link } from 'react-router-dom';
import { Globe, Mail, Share2 } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-surface-container-low w-full py-16 px-8 border-t border-primary/5">
      <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-12">
        <div className="space-y-6">
          <Link to="/" className="text-xl font-bold text-primary font-headline">Sipora</Link>
          <p className="text-on-surface-variant text-sm leading-relaxed max-w-xs">
            Revolutionizing hydration through the science of retronasal olfaction. Drink different.
          </p>
          <div className="flex gap-4">
            <a href="#" className="text-primary hover:text-primary-dim transition-colors"><Globe className="w-5 h-5" /></a>
            <a href="#" className="text-primary hover:text-primary-dim transition-colors"><Mail className="w-5 h-5" /></a>
            <a href="#" className="text-primary hover:text-primary-dim transition-colors"><Share2 className="w-5 h-5" /></a>
          </div>
        </div>

        <div>
          <h4 className="text-primary font-bold mb-6 uppercase tracking-widest text-xs">Shop</h4>
          <ul className="space-y-4 text-on-surface-variant text-sm font-medium">
            <li><Link to="/shop" className="hover:text-primary transition-colors">Water Bottles</Link></li>
            <li><Link to="/shop" className="hover:text-primary transition-colors">Scent Pods</Link></li>
            <li><a href="#" className="hover:text-primary transition-colors">Accessories</a></li>
            <li><a href="#" className="hover:text-primary transition-colors">Gift Cards</a></li>
          </ul>
        </div>

        <div>
          <h4 className="text-primary font-bold mb-6 uppercase tracking-widest text-xs">Company</h4>
          <ul className="space-y-4 text-on-surface-variant text-sm font-medium">
            <li><a href="#" className="hover:text-primary transition-colors">Our Story</a></li>
            <li><a href="#" className="hover:text-primary transition-colors">Technology</a></li>
            <li><a href="#" className="hover:text-primary transition-colors">Sustainability</a></li>
            <li><a href="#" className="hover:text-primary transition-colors">Careers</a></li>
          </ul>
        </div>

        <div>
          <h4 className="text-primary font-bold mb-6 uppercase tracking-widest text-xs">Support</h4>
          <ul className="space-y-4 text-on-surface-variant text-sm font-medium">
            <li><Link to="/support" className="hover:text-primary transition-colors">Help Center</Link></li>
            <li><a href="#" className="hover:text-primary transition-colors">Shipping</a></li>
            <li><a href="#" className="hover:text-primary transition-colors">Returns</a></li>
            <li><Link to="/support" className="hover:text-primary transition-colors">Contact</Link></li>
          </ul>
        </div>
      </div>

      <div className="max-w-7xl mx-auto mt-20 pt-8 border-t border-primary/10 flex flex-col md:flex-row justify-between items-center gap-6 text-on-surface-variant text-sm">
        <p>© 2024 Sipora. All rights reserved.</p>
        <div className="flex gap-8 font-medium">
          <a href="#" className="hover:text-primary transition-colors">Privacy Policy</a>
          <a href="#" className="hover:text-primary transition-colors">Terms of Service</a>
          <a href="#" className="hover:text-primary transition-colors">Cookies</a>
        </div>
      </div>
    </footer>
  );
}
