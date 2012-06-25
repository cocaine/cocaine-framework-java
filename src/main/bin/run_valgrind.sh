valgrind --trace-children=yes --show-reachable=yes --leak-check=full java -Djava.library.path=./target/lib -cp target/classes ru.yandex.cocaine.dealer.Valgrind
