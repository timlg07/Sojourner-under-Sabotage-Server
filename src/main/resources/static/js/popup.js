class Popup {

    /** @typedef {{title:string, content:string, cta:string}} PopupText */
    /** @type {Map<string, PopupText|array<PopupText>>} */
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
        ['code editor introduction', [
            {
                title: 'The Code Editor',
                content: `<p><strong>Welcome to the code editor.</strong></p>
                          <p>It might look familiar to you as it's based on Visual Studio Code. You can use the 
                          same keyboard shortcuts and commands that you're used to from VSC.</p>`,
                cta: 'Next',
            },
            {
                title: 'But wait.. there are 2 editors?',
                content: `<p>The editor on the left contains the <strong>class under test (CUT)</strong>.<br>
                          Here you can read the code of the component you're currently inspecting.</p>
                          <p>The editor on the right is your <strong>test class</strong>.<br>
                          Here you can write your unit tests for the CUT using JUnit.</p>`,
                cta: 'Next',
            },
            {
                title: 'Running your tests',
                content: `<p>To try out your tests, hit the <strong>Run</strong>-button. <br>
                          It will execute the tests and show you the results at the bottom of the window.</p>
                          <p>You don't need to save manually as the editor has auto-save (and also always saves before 
                          execution or closing)</p>`,
                cta: `Let's start!`,
            },
        ]],
        ['component fixed', {
            title: 'Great job!',
            content: `<p>You've fixed the bug and saved the component from failure.</p>
                      <p>The component is now up and running again!</p>`,
            cta: 'Continue',
        }]
    ]);

    /** @type {Popup} */
    static #instance = null;

    /** @type {Array<Function>} */
    #onClose = [];
    /** @type {Array<Function>} */
    #onTransitionEnd = [];
    /** @type {false|{key:string,index:number}} */
    #multistep = false;

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
        if (Array.isArray(text)) {
            this.#multistep = { key: contentKey, index: 0 };
            this.#renderMultiStep();
        } else {
            this.#multistep = false;
            this.#render(text);
        }
        return this;
    }

    #renderMultiStep() {
        if (this.#multistep === false) return;
        const text = Popup.#text.get(this.#multistep.key)[this.#multistep.index];
        this.#render(text);
    }

    /**
     * @param {PopupText} text
     */
    #render(text) {
        this.heading.innerText = text.title;
        this.content.innerHTML = text.content;
        this.button.innerText = text.cta;
        this.element.ariaHidden = 'false';
    }

    close() {
        if (this.#multistep === false) {
            this.#close();
        } else {
            const steps = Popup.#text.get(this.#multistep.key).length;
            this.#multistep.index++;
            if (this.#multistep.index < steps) {
                this.element.ariaHidden = 'true'; // animate out
                this.element.addEventListener('transitionend', this.#renderMultiStep.bind(this), { once: true });
            } else {
                this.#multistep = false;
                this.#close();
            }
        }
    }

    #close() {
        this.element.addEventListener('transitionend', () => {
            this.#onTransitionEnd.forEach(fn => fn());
            this.#onTransitionEnd = [];
        }, { once: true });

        this.element.ariaHidden = 'true';
        this.#onClose.forEach(fn => fn());
        this.#onClose = [];
    }

    onClose(fn) {
        this.#onClose.push(fn);
    }

    onTransitionEnd(fn) {
        this.#onTransitionEnd.push(fn);
    }
}
