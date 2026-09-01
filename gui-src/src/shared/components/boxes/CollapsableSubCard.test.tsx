import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CollapsableSubCard from './CollapsableSubCard';

// Mock lucide-react icon
vi.mock('lucide-react', () => ({ ChevronDown: () => <span data-testid="icon-chevron" /> }));

describe('CollapsableSubCard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('affiche le contenu enfant quand expanded=true (défaut)', () => {
    render(<CollapsableSubCard title="Titre"><div>Contenu</div></CollapsableSubCard>);
    expect(screen.getByText('Contenu')).toBeInTheDocument();
    expect(screen.getByText('Titre')).toBeInTheDocument();
  });

  it('utilise le fallbackText quand pas d’enfants', () => {
    render(<CollapsableSubCard title="Titre" fallbackText="Fallback" />);
    expect(screen.getByText('Fallback')).toBeInTheDocument();
  });

  it('toggle masque et ré-affiche le contenu', () => {
    render(<CollapsableSubCard title="Titre"><p>Bloc</p></CollapsableSubCard>);
    const button = screen.getByRole('button', { name: /Titre/i });
    // Premier clic -> collapse (contenu masqué via classe opacity-0). On vérifie disparition DOM visible
    fireEvent.click(button);
    // Contenu peut rester dans DOM mais avec opacity-0; on vérifie la classe sur wrapper grid
    const gridWrapper = button.parentElement!.nextElementSibling as HTMLElement;
    expect(gridWrapper.className).toMatch(/opacity-0/);
    // Second clic -> expand
    fireEvent.click(button);
    expect(gridWrapper.className).toMatch(/opacity-100/);
  });

  it('accepte un titre JSX', () => {
    render(<CollapsableSubCard title={<span>MonTitre</span>}><span>Texte</span></CollapsableSubCard>);
    expect(screen.getByText('MonTitre')).toBeInTheDocument();
  });
});

