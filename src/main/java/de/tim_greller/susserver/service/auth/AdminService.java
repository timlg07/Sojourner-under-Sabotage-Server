package de.tim_greller.susserver.service.auth;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.tim_greller.susserver.dto.UserRegistrationDTO;
import de.tim_greller.susserver.exception.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;

    public List<List<String>> createAccounts(int amount) {
        return IntStream.range(0, amount).mapToObj(i -> {
            while (true) {
                int randomNameIndex = (int) (Math.random() * Math.max(1_000, amount));
                var astronaut = getRandomAstronaut();
                var register = UserRegistrationDTO.builder()
                        .firstName(astronaut.get(0))
                        .lastName(astronaut.get(1))
                        .password(getRandomPassword())
                        .username(astronaut.get(2) + randomNameIndex)
                        .build();
                try {
                    userService.registerNewUserAccount(register);
                } catch (UserAlreadyExistException e) {
                    continue;
                }
                return List.of(
                        register.getFirstName() + " " + register.getLastName(),
                        register.getUsername(), register.getPassword()
                );
            }
        }).collect(Collectors.toList());
    }

    private String getRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private List<String> getRandomAstronaut() {
        int randomIndex = (int) (Math.random() * astronauts.size());
        return astronauts.get(randomIndex);
    }

    private static final List<List<String>> astronauts = List.<List<String>>of(
            List.of("Yuri", "Gagarin", "gagarin"),
            List.of("John", "Glenn", "glenn"),
            List.of("Scott", "Carpenter", "carpenter"),
            List.of("Wally", "Schirra", "schirra"),
            List.of("Gordon", "Cooper", "cooper"),
            List.of("Alan", "Shepard", "shepard"),
            List.of("Virgil", "Grissom", "grissom"),
            List.of("Roger", "Chaffee", "chaffee"),
            List.of("Edward", "White", "white"),
            List.of("James", "McDivitt", "mcdivitt"),
            List.of("Frank", "Borman", "borman"),
            List.of("Jim", "Lovell", "lovell"),
            List.of("Thomas", "Stafford", "stafford"),
            List.of("John", "Young", "young"),
            List.of("Michael", "Collins", "collins"),
            List.of("Walter", "Cunningham", "cunningham"),
            List.of("Rusty", "Schweickart", "schweickart"),
            List.of("Donn", "Eisele", "eisele"),
            List.of("John", "Swigert", "swigert"),
            List.of("Stuart", "Roosa", "roosa"),
            List.of("Paul", "Weitz", "weitz"),
            List.of("Alfred", "Worden", "worden"),
            List.of("Alan", "Bean", "bean"),
            List.of("Jack", "Lousma", "lousma"),
            List.of("William", "Shepherd", "shepherd"),
            List.of("Eugene", "Kranz", "kranz"),
            List.of("Edgar", "Mitchell", "mitchell"),
            List.of("Joseph", "Kerwin", "kerwin"),
            List.of("Ronald", "Evans", "evans"),
            List.of("Gerald", "Carr", "carr"),
            List.of("Edward", "Gibson", "gibson"),
            List.of("William", "Pogue", "pogue"),
            List.of("Owen", "Garriott", "garriott"),
            List.of("Don", "Lind", "lind"),
            List.of("Franklin", "Musgrave", "musgrave"),
            List.of("Donald", "Peterson", "peterson"),
            List.of("Kathryn", "Thornton", "thornton"),
            List.of("Richard", "Covey", "covey"),
            List.of("James", "van Hoften", "vanhoften"),
            List.of("Richard", "Truly", "truly"),
            List.of("Charles", "Bolden", "bolden"),
            List.of("Richard", "Hieb", "hieb"),
            List.of("Margaret", "Seddon", "seddon"),
            List.of("Ronald", "Grabe", "grabe"),
            List.of("David", "Walker", "walker"),
            List.of("Norman", "Thagard", "thagard"),
            List.of("James", "Buchli", "buchli"),
            List.of("Mary", "Cleave", "cleave"),
            List.of("Jake", "Garn", "garn"),
            List.of("Loren", "Acton", "acton"),
            List.of("Roy", "Bridges", "bridges"),
            List.of("Karl", "Henize", "henize"),
            List.of("Jerry", "Ross", "ross"),
            List.of("Anthony", "England", "england"),
            List.of("Loren", "Shriver", "shriver"),
            List.of("David", "Leestma", "leestma"),
            List.of("Guion", "Bluford", "bluford"),
            List.of("Francis", "Scobee", "scobee"),
            List.of("Judith", "Resnick", "resnick"),
            List.of("Michael", "Smith", "smith"),
            List.of("Sharon", "McAuliffe", "mcauliffe"),
            List.of("Ellison", "Onizuka", "onizuka"),
            List.of("Gregory", "Jarvis", "jarvis"),
            List.of("Ronald", "McNair", "mcnair"),
            List.of("Judith", "Resnik", "resnik"),
            List.of("Paul", "Scully-Power", "scullypower"),
            List.of("Steven", "Nagel", "nagel"),
            List.of("Patrick", "Baudry", "baudry"),
            List.of("Jean-Loup", "Chretien", "chretien"),
            List.of("Alexander", "Viktorenko", "viktorenko"),
            List.of("Ulf", "Merbold", "merbold"),
            List.of("Claude", "Furrer", "furrer"),
            List.of("Claude", "Nicollier", "nicollier"),
            List.of("Marsha", "Ivins", "ivins"),
            List.of("David", "Hilmers", "hilmers"),
            List.of("Shannon", "Lucid", "lucid"),
            List.of("Jean-Jacques", "Favier", "favier"),
            List.of("Thomas", "Akers", "akers"),
            List.of("Michel", "Tognini", "tognini"),
            List.of("Bruce", "Melnick", "melnick"),
            List.of("Michael", "Baker", "baker"),
            List.of("James", "Adamson", "adamson"),
            List.of("James", "Low", "low"),
            List.of("Michael", "Foale", "foale"),
            List.of("Kenneth", "Bowersox", "bowersox"),
            List.of("Catherine", "Coleman", "coleman"),
            List.of("Richard", "Searfoss", "searfoss"),
            List.of("Brian", "Duffy", "duffy"),
            List.of("Donald", "McMonagle", "mcmonagle"),
            List.of("Janice", "Voss", "voss"),
            List.of("Mario", "Runco", "runco"),
            List.of("Rick", "Husband", "husband"),
            List.of("William", "McCool", "mccool"),
            List.of("Kalpana", "Chawla", "chawla"),
            List.of("David", "Brown", "brown"),
            List.of("Michael", "Anderson", "anderson"),
            List.of("Laurel", "Clark", "clark"),
            List.of("Ilan", "Ramon", "ramon"),
            List.of("Daniel", "Bursch", "bursch"),
            List.of("Rex", "Walheim", "walheim"),
            List.of("Steven", "Hawley", "hawley"),
            List.of("Lee", "Bowman", "bowman"),
            List.of("Joseph", "Tanner", "tanner"),
            List.of("Carlos", "Noriega", "noriega"),
            List.of("Stephen", "Robinson", "robinson"),
            List.of("Thomas", "Marshburn", "marshburn"),
            List.of("George", "Zamka", "zamka"),
            List.of("Pamela", "Melroy", "melroy"),
            List.of("Daniel", "Tani", "tani"),
            List.of("Heidemarie", "Stefanyshyn-Piper", "stefanyshynpiper"),
            List.of("Edward", "Fincke", "fincke"),
            List.of("Fyodor", "Yurchikhin", "yurchikhin"),
            List.of("John", "Olivas", "olivas"),
            List.of("Patrick", "Forrester", "forrester"),
            List.of("Steven", "Swanson", "swanson"),
            List.of("Richard", "Arnold", "arnold"),
            List.of("John", "Philips", "philips"),
            List.of("Donald", "Pettit", "pettit"),
            List.of("Barbara", "Morgan", "morgan"),
            List.of("Ronald", "Garan", "garan"),
            List.of("Michael", "Fossum", "fossum"),
            List.of("Terry", "Virts", "virts"),
            List.of("Douglas", "Wheelock", "wheelock"),
            List.of("Christopher", "Cassidy", "cassidy"),
            List.of("Tracy", "Glover", "glover"),
            List.of("Kathleen", "Rubins", "rubins"),
            List.of("Robert", "Kimbrough", "kimbrough"),
            List.of("Peggy", "Whitson", "whitson"),
            List.of("Jack", "Fischer", "fischer"),
            List.of("Thomas", "Pesquet", "pesquet"),
            List.of("Serena", "Aunaon-Chancellor", "aunanchancellor"),
            List.of("Scott", "Tingle", "tingle"),
            List.of("Jeanette", "Epps", "epps"),
            List.of("Tyler", "Hague", "hague"),
            List.of("Christina", "Koch", "koch"),
            List.of("Jessica", "Meir", "meir"),
            List.of("Andrew", "Morgan", "morgan"),
            List.of("Robert", "Behnken", "behnken"),
            List.of("Douglas", "Hurley", "hurley"),
            List.of("Frank", "Cassada", "cassada"),
            List.of("Hayley", "Sembroski", "sembroski"),
            List.of("Jared", "Isaacman", "isaacman"),
            List.of("Megan", "Proctor", "proctor"),
            List.of("Charles", "Hobaugh", "hobaugh"),
            List.of("Kjell", "Lindgren", "lindgren"),
            List.of("Koichi", "Wakata", "wakata"),
            List.of("Andre", "Kuipers", "kuipers"),
            List.of("Leroy", "Chiao", "chiao"),
            List.of("Salizhan", "Sharipov", "sharipov"),
            List.of("Yuri", "Shargin", "shargin"),
            List.of("Pavel", "Vinogradov", "vinogradov"),
            List.of("Sergei", "Krikalev", "krikalev"),
            List.of("Gennady", "Strekalov", "strekalov"),
            List.of("Vladimir", "Dezhurov", "dezhurov"),
            List.of("Sergei", "Volkov", "volkov"),
            List.of("Yelena", "Kondakova", "kondakova"),
            List.of("Aleksandr", "Polishchuk", "polishchuk"),
            List.of("Talgat", "Musabayev", "musabayev"),
            List.of("Yuri", "Baturin", "baturin"),
            List.of("Anatoly", "Solovyev", "solovyev"),
            List.of("Aleksandr", "Balandin", "balandin"),
            List.of("Vasily", "Zholobov", "zholobov"),
            List.of("Aleksandr", "Kaleri", "kaleri"),
            List.of("Yuri", "Usachev", "usachev"),
            List.of("Yuri", "Gidzenko", "gidzenko"),
            List.of("Sergei", "Krikalev", "krikalev"),
            List.of("Valeri", "Tsibliyev", "tsibliyev"),
            List.of("Musa", "Musalimov", "musalimov"),
            List.of("Anatoly", "Vinogradov", "vinogradov"),
            List.of("Aleksandr", "Lazutkin", "lazutkin"),
            List.of("Gennady", "Padalka", "padalka"),
            List.of("Valery", "Korzun", "korzun"),
            List.of("Sergei", "Treschev", "treschev"),
            List.of("Salijan", "Sharipov", "sharipov"),
            List.of("Fyodor", "Yurchikhin", "yurchikhin"),
            List.of("Aleksandr", "Skvortsov", "skvortsov"),
            List.of("Oleg", "Kononenko", "kononenko"),
            List.of("Mikhail", "Korniyenko", "korniyenko"),
            List.of("Anton", "Shkaplerov", "shkaplerov"),
            List.of("Anatoly", "Ivanishin", "ivanishin"),
            List.of("Pyotr", "Vagner", "vagner"),
            List.of("Sergey", "Ryazansky", "ryazansky"),
            List.of("Oleg", "Artemyev", "artemyev"),
            List.of("Oleg", "Novitskiy", "novitskiy"),
            List.of("Aleksandr", "Samokutyayev", "samokutyayev"),
            List.of("Yelena", "Serova", "serova"),
            List.of("Sergey", "Shaydobin", "shaydobin"),
            List.of("Aleksey", "Ovchinin", "ovchinin"),
            List.of("Oleg", "Skripochka", "skripochka"),
            List.of("Aleksandr", "Misurkin", "misurkin"),
            List.of("Yulia", "Peresild", "peresild"),
            List.of("Dmitri", "Prokopyev", "prokopyev"),
            List.of("Andrey", "Petelin", "petelin"),
            List.of("Sergey", "Korsakov", "korsakov"),
            List.of("Francisco", "Rubio", "rubio")
    );
}
