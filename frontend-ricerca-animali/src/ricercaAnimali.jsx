import React, { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { createPortal } from 'react-dom';

// Barra di ricerca + elenco degli animali disponibili, in React (bundle
// compilato con Vite, vedi vite.config.js in questa cartella).
//
// Ricerca "live": ogni volta che il testo cambia, dopo una piccola pausa
// (debounce, per non interrogare il server ad ogni singolo tasto) chiama
// /api/animali/ricerca e sostituisce l'elenco sotto la barra, senza
// ricaricare la pagina e senza bisogno di premere Invio o il bottone "Cerca".
//
// Il form (mount principale) e l'elenco sono in due punti diversi della
// pagina (la barra in alto a destra nell'header, l'elenco sotto): per
// questo l'elenco viene "portato" (React portal) dentro #risultati-animali-root,
// che esiste gia' nell'HTML, invece di montare due root React separate.

const DEBOUNCE_MS = 300;

// Stessa logica di Thymeleaf #strings.abbreviate(testo, 120): se il testo e'
// piu' lungo del massimo, lo taglia e aggiunge "...".
function abbrevia(testo, massimo) {
    if (testo == null) {
        return null;
    }
    if (testo.length <= massimo) {
        return testo;
    }
    return testo.slice(0, Math.max(0, massimo - 3)) + '...';
}

function infoBadge(stato) {
    switch (stato) {
        case 'DISPONIBILE':
            return { classe: 'badge-disponibile', testo: 'Disponibile' };
        case 'IN_VALUTAZIONE':
            return { classe: 'badge-in-valutazione', testo: 'In valutazione' };
        case 'ADOTTATO':
            return { classe: 'badge-adottato', testo: 'Adottato' };
        default:
            return null;
    }
}

function CardAnimale({ animale }) {
    const badge = infoBadge(animale.stato);
    const descrizioneAbbreviata = abbrevia(animale.descrizione, 120);

    return (
        <div className="animal-card">
            {badge && <span className={'badge ' + badge.classe}>{badge.testo}</span>}

            <h3>
                <a href={'/animali/' + animale.id}>{animale.nome}</a>
            </h3>

            <div className="animal-meta">
                <span className="animal-tag">{animale.specie}</span>
                {animale.razza && <span className="animal-tag">{animale.razza}</span>}
                {animale.eta != null && <span className="animal-tag">{animale.eta} anni</span>}
                {animale.sesso && <span className="animal-tag">{animale.sesso}</span>}
            </div>

            {descrizioneAbbreviata && <p className="animal-desc">{descrizioneAbbreviata}</p>}
        </div>
    );
}

function ElencoAnimali({ animali }) {
    if (animali.length === 0) {
        return <p>Al momento non ci sono animali disponibili per la ricerca effettuata.</p>;
    }
    return (
        <div className="card-grid">
            {animali.map((animale) => (
                <CardAnimale key={animale.id} animale={animale} />
            ))}
        </div>
    );
}

function AppRicercaAnimali({ valoreIniziale, animaliIniziali, apiRicerca, contenitoreRisultati }) {
    const [testo, setTesto] = useState(valoreIniziale || '');
    const [animali, setAnimali] = useState(animaliIniziali || []);
    const numeroRichiesta = useRef(0);
    const primaRenderizzazione = useRef(true);

    useEffect(() => {
        // Al primo render l'elenco e' gia' quello giusto (passato dal server,
        // vedi animaleListJson in AnimaleController): niente da richiedere.
        if (primaRenderizzazione.current) {
            primaRenderizzazione.current = false;
            return;
        }

        const testoPulito = testo.trim();
        const idRichiesta = ++numeroRichiesta.current;

        const timer = setTimeout(() => {
            const url = apiRicerca + (testoPulito !== '' ? '?cerca=' + encodeURIComponent(testoPulito) : '');
            fetch(url)
                .then((risposta) => {
                    if (!risposta.ok) {
                        throw new Error('Richiesta di ricerca fallita: ' + risposta.status);
                    }
                    return risposta.json();
                })
                .then((risultati) => {
                    // Se nel frattempo l'utente ha digitato altro ed e' partita una
                    // ricerca piu' recente, ignora questa risposta arrivata in ritardo.
                    if (idRichiesta === numeroRichiesta.current) {
                        setAnimali(risultati);
                    }
                })
                .catch((errore) => {
                    console.error('Errore nella ricerca animali:', errore);
                });
        }, DEBOUNCE_MS);

        return () => clearTimeout(timer);
    }, [testo, apiRicerca]);

    function handleSubmit(evento) {
        // La ricerca e' gia' "live" ad ogni carattere digitato: il submit non
        // deve fare nulla, serve solo a evitare che la pagina si ricarichi se
        // si preme Invio nel campo di testo.
        evento.preventDefault();
    }

    return (
        <>
            <form className="search-bar search-bar-compact" onSubmit={handleSubmit}>
                <input
                    type="text"
                    name="cerca"
                    id="cerca-animali"
                    value={testo}
                    onChange={(evento) => setTesto(evento.target.value)}
                    placeholder="Cerca…"
                    aria-label="Cerca animale"
                />
                <button type="submit">Cerca</button>
            </form>
            {createPortal(<ElencoAnimali animali={animali} />, contenitoreRisultati)}
        </>
    );
}

function avvia() {
    const contenitoreForm = document.getElementById('ricerca-animali-root');
    const contenitoreRisultati = document.getElementById('risultati-animali-root');
    if (!contenitoreForm || !contenitoreRisultati) {
        return;
    }

    const valoreIniziale = contenitoreForm.dataset.valoreIniziale || '';
    const apiRicerca = contenitoreForm.dataset.apiRicerca || '/api/animali/ricerca';
    const animaliIniziali = window.__ANIMALI_INIZIALI__ || [];

    createRoot(contenitoreForm).render(
        <AppRicercaAnimali
            valoreIniziale={valoreIniziale}
            animaliIniziali={animaliIniziali}
            apiRicerca={apiRicerca}
            contenitoreRisultati={contenitoreRisultati}
        />
    );
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', avvia);
} else {
    avvia();
}
