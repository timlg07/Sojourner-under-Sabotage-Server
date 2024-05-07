class OffViewportInfo {
    constructor(selector) {
        this.container = document.querySelector(selector);
        this.objective = this.container.querySelector('.visible-content');
        this.hoverText = this.container.querySelector('.hover-content');

        this.container.addEventListener('mouseenter', this.show.bind(this));
        this.container.addEventListener('mouseleave', this.hide.bind(this));
        this.hide();

        const unity = document.getElementById('unity-canvas');
        this.container.addEventListener('click', () => unity.focus());
    }

    hide() {
        this.container.style.top = (-1 * this.hoverText.getBoundingClientRect().height) + 'px';
        this.container.classList.remove('is-expanded');
    }

    show() {
        this.container.style.top = '0';
        this.container.classList.add('is-expanded');
    }

    playUpdateAnimation() {
        this.show();
        setTimeout(this.hide.bind(this), 1e3);
    }
}

class ObjectiveDisplay extends OffViewportInfo {
    constructor(selector) {
        super(selector);

        es.registerHandler(GameProgressionChangedEvent.type, this.gameProgressionChanged.bind(this));
    }

    /**
     * @param {string} objective
     * @param {string} details
     */
    setObjective(objective, details) {
        this.objective.innerHTML = objective;
        this.hoverText.innerHTML = details;
        this.playUpdateAnimation();
    }

    getRoomName(roomId) {
        const rooms = [
            'Cryo Chamber', 'Engine Compartment', 'Farm', 'Defense', 'Kitchen and Dining Room', 'Reactor',
            'Infirmary', 'Communication', 'Escape Pod'
        ];
        return rooms[roomId - 1];
    }

    /** @param {{progression:UserGameProgressionDTO}} evt */
    gameProgressionChanged(evt) {
        switch (evt.progression.status) {
            case 'DOOR':
                this.setObjective(
                    'Fix the door',
                    '<p>The door to the next room is broken.<br> Find it and fix it by connecting the wires so that' +
                    'current can flow through them from the energy source to the door opening mechanism.</p>' +
                    '<p>Click on the wires to rotate them.</p>' +
                    '<p>Once the circuit is complete, the current can flow through the wires from the top left to ' +
                    'the bottom right and the door will open.</p>'
                );
                break;
            case 'TALK':
                this.setObjective(
                    'Talk to the robot',
                    'You\'re now in the '+this.getRoomName(evt.progression.room)+'! ' +
                    'Talk to the robot and find out more about this room.'
                );
                break;
            case 'TEST':
                this.setObjective(
                    'Write tests for the component.',
                    '<p>Find the component and open the code editor.</p><p>' +
                    'Write tests for the component to make sure it works as expected. Using these tests, you\'ll be ' +
                    'able to automatically detect if the component breaks in the future.</p>' +
                    '<p>The lines highlighted in green are covered by your tests.<br><span id="coverage-info"></span></p>'
                );
                break;
            case 'TESTS_ACTIVE':
                this.setObjective(
                    'Find out more about this room',
                    'Your tests are now active, so you don\'t have to worry about the component breaking. ' +
                    'Once the component is attacked, your tests will detect it and you can fix it.'
                );
                break;
            case 'MUTATED':
                this.setObjective(
                    'Fix the mutated component',
                    '<p>The component has been attacked! Luckily your tests were in place and detected it soon enough. ' +
                    '</p><p>Find the mutated component and fix it.</p><p>' +
                    'The code was modified by the attacker, so you need to find the bug in it and fix it.</p>'
                );
                break;
            case 'DESTROYED':
                this.setObjective(
                    'Fix the destroyed component',
                    '<p>The component has been attacked. Your tests were not good enough to detect the mutation.' +
                    'Therefore, the component was destroyed.</p><p>' +
                    'The code was modified by the attacker, so you need to find the bug in it and fix it.</p><p>' +
                    'The robot will write some tests for you, which you can use to ensure that the component ' +
                    'is working as expected again.</p>'
                );
                break;
            case 'DEBUGGING':
                this.setObjective(
                    'Find the bug and fix it',
                    '<p>The components code (on the left) was mutated by the attacker. Find the bug and fix it.</p><p>' +
                    'You can use <code>System.out.println(...);</code> to print out values and strings while you\'re ' +
                    'debugging the code.</p><p> You can also write new or modify old tests to try out different scenarios.</p>'
                );
                break;
        }
    }
}
