Чтобы собрать переходим в каталог labyrinth/src и выполняем слудующую команду:
`javac -cp "../../lib/jar/lwjgl.jar:." com/github/graphics/labyrinth/Application.java`

Затем, чтобы запустить выполняем из той же директории:
`java -XstartOnFirstThread -cp "../../lib/jar/lwjgl.jar:." -Djava.library.path="../../lib/native" com.github.graphics.labyrinth.Application`
