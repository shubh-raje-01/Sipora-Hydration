import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Menu, Search, ShoppingCart, User, X } from 'lucide-react';
import { cn } from '../lib/utils';
import { AnimatePresence, motion } from 'motion/react';
import { useCart } from '../context/CartContext';

const navLinks = [
  { name: 'Shop', path: '/shop' },
  { name: 'Technology', path: '/#how-it-works' },
  { name: 'Support', path: '/support' },
];

export default function Navbar() {
  const location = useLocation();
  const isCheckout = location.pathname === '/checkout';
  const { cartCount } = useCart();
  const [menuOpen, setMenuOpen] = React.useState(false);

  React.useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname, location.hash]);

  return (
    <nav className="fixed top-0 z-50 w-full border-b border-primary/10 glass-panel">
      <div className="max-w-7xl mx-auto px-6 md:px-8 py-4 flex justify-between items-center">
        <div className="flex items-center gap-12">
          <Link to="/" className="text-2xl font-black tracking-tight text-primary font-headline">
            Sipora
          </Link>
          {!isCheckout && (
            <div className="hidden md:flex gap-8">
              {navLinks.map((link) => (
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
              <Link to="/shop" className="bg-primary text-white px-6 py-2 rounded-full font-black text-sm hover:bg-primary-dim transition-colors shadow-lg shadow-primary/20">
                Shop
              </Link>
              <button
                type="button"
                className="flex h-10 w-10 items-center justify-center rounded-full bg-white text-primary shadow-lg shadow-primary/10 transition hover:bg-secondary-container md:hidden"
                onClick={() => setMenuOpen((open) => !open)}
                aria-label={menuOpen ? 'Close menu' : 'Open menu'}
                aria-expanded={menuOpen}
              >
                {menuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
              </button>
            </>
          )}
        </div>
      </div>
      <AnimatePresence>
        {menuOpen && !isCheckout && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-x-0 top-[4.5rem] z-40 px-4 pb-4 md:hidden"
          >
            <motion.div
              initial={{ y: -16, scale: 0.98 }}
              animate={{ y: 0, scale: 1 }}
              exit={{ y: -16, scale: 0.98 }}
              transition={{ duration: 0.22, ease: 'easeOut' }}
              className="overflow-hidden rounded-lg border border-primary/10 bg-primary text-white shadow-2xl shadow-primary/30"
            >
              <div className="space-y-1 p-4">
                {navLinks.map((link, index) => (
                  <motion.div
                    key={link.name}
                    initial={{ opacity: 0, x: -18 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -12 }}
                    transition={{ delay: index * 0.06, duration: 0.22 }}
                  >
                    <Link
                      to={link.path}
                      className="flex items-center justify-between rounded-lg px-4 py-4 text-2xl font-black tracking-tight transition hover:bg-white/10"
                    >
                      {link.name}
                      <span className="h-2 w-2 rounded-full bg-secondary" />
                    </Link>
                  </motion.div>
                ))}
              </div>
              <motion.div
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 8 }}
                transition={{ delay: 0.2, duration: 0.22 }}
                className="grid grid-cols-2 gap-3 border-t border-white/10 p-4"
              >
                <button className="flex items-center justify-center gap-2 rounded-full bg-white/10 px-4 py-3 text-sm font-black text-white">
                  <Search className="h-4 w-4" />
                  Search
                </button>
                <Link to="/shop" className="flex items-center justify-center rounded-full bg-secondary px-4 py-3 text-sm font-black text-primary">
                  Shop now
                </Link>
              </motion.div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
}
