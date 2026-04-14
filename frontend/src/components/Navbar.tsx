import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Search, ShoppingCart, User } from 'lucide-react';
import { cn } from '../lib/utils';
import { motion } from 'motion/react';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const location = useLocation();
  const isCheckout = location.pathname === '/checkout';
  const { cartCount } = useCart();

  return (
    <nav className="fixed top-0 w-full z-50 glass-panel border-b border-primary/5">
      <div className="max-w-7xl mx-auto px-6 md:px-8 py-4 flex justify-between items-center">
        <div className="flex items-center gap-12">
          <Link to="/" className="text-2xl font-bold tracking-tighter text-primary font-headline">
            Sipora
          </Link>
          {!isCheckout && (
            <div className="hidden md:flex gap-8">
              {[
                { name: 'Shop', path: '/shop' },
                { name: 'Technology', path: '/#tech' },
                { name: 'Support', path: '/support' },
              ].map((link) => (
                <Link
                  key={link.name}
                  to={link.path}
                  className={cn(
                    "text-sm font-bold transition-colors hover:text-primary",
                    location.pathname === link.path ? "text-primary" : "text-on-surface-variant"
                  )}
                >
                  {link.name}
                </Link>
              ))}
            </div>
          )}
        </div>

        <div className="flex items-center gap-6">
          {isCheckout ? (
            <div className="flex items-center gap-2 text-sm font-medium text-on-surface-variant">
              <span>Secure Checkout</span>
              <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                <Search className="w-4 h-4" />
              </div>
            </div>
          ) : (
            <>
              <div className="hidden sm:flex items-center gap-4 text-on-surface-variant">
                <button className="p-2 hover:text-primary transition-colors">
                  <Search className="w-5 h-5" />
                </button>
                <button className="p-2 hover:text-primary transition-colors">
                  <User className="w-5 h-5" />
                </button>
              </div>
              <Link to="/checkout" className="relative p-2 text-on-surface-variant hover:text-primary transition-colors">
                <ShoppingCart className="w-5 h-5" />
                {cartCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-primary text-white text-[10px] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                    {cartCount}
                  </span>
                )}
              </Link>
              <Link to="/shop" className="bg-primary text-white px-6 py-2 rounded-full font-bold text-sm hover:bg-primary-dim transition-colors shadow-lg shadow-primary/20">
                Shop Now
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
