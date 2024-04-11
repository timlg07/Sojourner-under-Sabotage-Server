class Popup {

    /** @type {Map<string, {title:string, content:string, cta:string}>} */
    static #text = new Map([
        ['welcome', {
            title: 'Welcome to Sojourner under Sabotage!',
            content: `<p>You're on board the spaceship <i>Sojourner</i>...</p>`,
            cta: 'Begin the journey',
        }],
        ['start debugging', {
            title: 'The Component was Attacked!',
            content: `<p>The component has been mutated. You need to find the bug and fix it.</p>`,
            cta: 'Start Debugging',
        }],
    ]);

    /** @type {Popup} */
    static #instance = null;

    /** @type {Array<Function>} */
    #onClose = [];

    static get instance() {
        if (Popup.#instance === null) {
            Popup.#instance = new Popup();
        }
        return Popup.#instance;
    }

    constructor() {
        this.element = document.getElementById('popup');
        this.heading = this.element.querySelector('.heading');
        this.content = this.element.querySelector('.content');
        this.button  = this.element.querySelector('#continue-button');
        this.button.addEventListener('click', this.close.bind(this));
    }

    open(contentKey) {
        if (Popup.#text.has(contentKey) === false) {
            throw new Error(`No content found for key ${contentKey}`);
        }
        const text = Popup.#text.get(contentKey);
        this.heading.innerText = text.title;
        this.content.innerHTML = text.content;
        this.button.innerText = text.cta;
        this.element.ariaHidden = 'false';
        return this;
    }

    close() {
        this.element.ariaHidden = 'true';
        this.#onClose.forEach(fn => fn());
        this.#onClose = [];
    }

    onClose(fn) {
        this.#onClose.push(fn);
    }
}
