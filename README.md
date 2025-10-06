Relazione progetto – Gioco stile SonicX
1. Abstract
	Questo progetto consiste nello sviluppo di un videogioco in Java ispirato a Sonic the Hedgehog.
	L'obiettivo è stato quello di realizzare un piccolo motore di gioco 2D che includesse il movimento
	del personaggio principale (Sonic), nemici, livelli con ostacoli e piattaforme, gestione delle collisioni,
	HUD con punteggio e tempo, oltre ad una semplice interfaccia grafica di menu. Il lavoro ha
	permesso di applicare concetti di programmazione orientata agli oggetti, progettazione UML e
	pattern di design.
2. Analisi
	2.1 Requisiti
		Requisiti funzionali:
			• Movimento del personaggio principale (corsa, salto, interazione con molle e piattaforme).
			• Gestione di nemici con logiche differenti.
			• Gestione dei livelli con ostacoli, obiettivi e fine livello.
			• HUD con punteggio, tempo e vite residue.
			• Menu iniziale e interfaccia di gioco.
			• Musica ed effetti sonori.
		Requisiti non funzionali:
			• Portabilità su Java 8+.
			• Struttura modulare a pacchetti.
			• Scrittura del codice semplice, leggibile e con componenti facilmente riutilizzabili.
			• Buone prestazioni su PC standard.
	2.2 Analisi e modello del dominio
		Il dominio del gioco comprende entità principali quali Player, Enemy, Projectile, Shield, Level,
		CollisionManager, Spring, MovingPlatform e FinishGate. Il giocatore interagisce con elementi del
		mondo (molle, piattaforme, nemici), mentre il CollisionManager è responsabile di verificare e
		risolvere le interazioni. Di seguito è riportato lo schema UML del dominio che evidenzia le principali
		entità e le loro relazioni.
3. Design
	3.1 Architettura
		L'architettura segue uno stile Model-View-Controller semplificato: Model: entità di gioco (Player,
		Enemy, Projectile, Shield, Level, CollisionManager). View: classi di rendering e HUD (HUD,
		MenuSonicGrafico). Controller: GameApp come gestore principale, InputHandler per input da
		tastiera.
	3.2 Design dettagliato
		Alcuni pattern utilizzati: Strategy: gli stati del Player (State) rappresentano strategie di
		comportamento differenti, come camminare, saltare o entrare in stato di invulnerabilità. Observer:
		l'HUD osserva lo stato del Player per aggiornare punteggio, vite e tempo. Template Method: i livelli
		(Level1, Level2, Level3) estendono Level sovrascrivendo solo alcune parti (layout, nemici),
		mantenendo invariata la logica base.
4. Sviluppo
	4.1 Testing automatizzato
		Sono stati realizzati test JUnit di base su CollisionManager e Player, verificando che le collisioni e il
		conteggio delle vite funzionino correttamente.
	4.2 Note di sviluppo
		• Utilizzo di enum Axis per rappresentare il movimento delle piattaforme mobili.
		• Utilizzo di Timer per gestire animazioni e invulnerabilità temporanea.
		• Metodo bounceOnSpring(Spring s) del Player per simulare la fisica della molla.
		• Gestione delle collisioni centralizzata in CollisionManager con dispatch basato sul tipo di entità.
		• Uso di RandomUtil per variare nemici o oggetti generati.
5. Commenti finali
	5.1 Autovalutazione e lavori futuri
		Mi sono occupato di: Design delle entità e gestione delle collisioni. Punti di forza: codice
		riutilizzabile e modulare. Da migliorare: documentazione. Sviluppo dei livelli e sistema HUD.
		Punti di forza: interfaccia chiara. Da migliorare: separazione logica/grafica. Gestione dell’input e
		interfaccia menu. Punti di forza: semplicità di utilizzo. Da migliorare: test automatici. Integrazione
		musica ed effetti sonori. Punti di forza: immersione del gameplay. Da migliorare: gestione
		asincrona audio.
	5.2 Difficoltà incontrate
		• Integrazione tra fisica e rendering.
		• Gestione corretta delle collisioni con molte entità contemporanee.
		• Sincronizzazione tra logica di gioco e thread grafico.
6. Guida utente
	Per avviare il gioco, eseguire la classe game.Main.
	Comandi disponibili: Frecce direzionali: muovere il personaggio. Spazio: salto. Invio: avvio di una
	partita dal menu. Obiettivo del gioco: raggiungere il FinishGate alla fine di ogni livello, evitando i
	nemici e raccogliendo oggetti bonus.
