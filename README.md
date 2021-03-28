# Flopbox API

Tristan Coignion

28/03/2021

## Introduction

Flopbox est une plateforme écrite en Java qui expose une API REST permettant de communiquer avec des serveurs FTP.

## [Video de présentation](https://youtu.be/uoqIJ8hsSDo)

## Installation

Pour installer l'application utilisez la commande :

```shell
mvn clean package -Dmaven.test.skip=true
```

puis

```shell
java -jar target/flopbpox-1.0.0.jar
```

Par defaut, le serveur se lance sur le port 8080.

Pour lancer les tests faites :

```shell
mvn test
```

## Documentation

La documentation de l'API, ainsi que les diagrammes UML utilisés se trouvent dans le dossier `/docs` et dans le
dossier `target/apicdocs` (celui ci est généré lors de la génération du jar)

Vous pouvez également accéder à la spécification OpenAPI de FlopBox à cette
addresse `http://localhost:8080/v3/api-docs/`.

Mais encore mieux, vous pouvez accéder à la spécification avec une UI ici `http://localhost:8080/swagger-ui`

## Architecture

L'application est séparée en package de *topics*

    - user : La gestion des utilisateurs
    - server : La gestion des serveurs FTP
    - security : Gérer l'authentification des utilisateurs
    - ftp : Intéragir avec les serveurs FTP
    - exceptions : Exceptions globales

Dans les packages `user`, `server`, `security` et `ftp` se trouvent des **Controllers** qui recoivent des requêtes HTTP
et y répondent, ils délèguent le traitement de la requête à des **Services** qui lient tous les acteurs de l'application
pour y répondre.

Dans `user` et `server` se trouvent les objets designés par le package `User.java` et `Server.java` ainsi que des **
Repositories** permettant de communiquer avec la base de données pour récupérer et sauvegarder les objets.

Dans `ftp`, on pourra retrouver l'interface `FTPConnector` qui donne un contrat pour interagir avec un serveur FTP. Elle
est implémentée par `FTPConnectorImpl`. C'est dans cette classe que seront fait les appels au serveur FTP grâce à la
classe `FTPSClient` de `apache.commons.net`.

#### La base de données

Les données sont stockées sur un fichier dans le dossier `data` grâce à une BDD `H2`

### Gestion d'erreur

## Code Samples

### Gestion de l'authentification

L'utilisateur doit s'authentifier dans l'application avec l'endpoint `/login`. Il reçoit alors un token, qu'il doit
mettre dans son header à chaque appel à un endpoint sécurisé. (Tout celà est rendu possible grâce à Spring Boot).

Le code en question se trouve dans le package `security`

### Méthodes lambda dans FTPConnectorImpl / Factorisation

J'ai factorisé l'envoi de commandes grâce à des fonction lambda paramétrées. C'est très pratique, et ça fait que toutes
mes méthodes pour envoyer des commandes au serveur FTP font maximum deux lignes.

`````java
public class FTPConnectorImpl {
	
	public List<FTPFile> list(Server server, String path, String username, String password) {
		return sendCommandWithReturn(server, username, password,
				(FTPClient ftpClient) -> Arrays.asList(ftpClient.listFiles(path)));
	}

	private <T> T sendCommandWithReturn(Server server, String username, String password,
										CommandWithReturn<T> operation) {
			FTPClient ftpClient = connectToServer(server, username, password);
			T data = operation.execute(ftpClient);
			safeDisconnect(ftpClient);
			return data;
	}
	
	protected interface CommandWithReturn<T> {
		T execute(FTPClient ftpClient) throws IOException;
	}
}
`````

### Documentation OpenAPI générée avec Spring

J'ai utilisé la librairie `springdoc-openapi-ui` qui me permet de générer ma documentation automatiquement en fonction
d'annotations que je met sur mes méthodes de Controller

`````java
public class FTPController {
	@Operation(summary = "Download a file",
			security = @SecurityRequirement(name = FlopBoxApplication.FLOPBOX_SECURITY),
			responses = {
					@ApiResponse(responseCode = "200", description = "File download", content = {
							@Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
					@ApiResponse(responseCode = "403", description = "Error during FTP operation"),
					@ApiResponse(responseCode = "404", description = "Server not found"),
					@ApiResponse(responseCode = "500", description = "Error while connecting to the FTP server")
			}
	)
	@GetMapping(FILES)
	public ResponseEntity<Resource> downloadFile(@PathVariable int serverId, @RequestParam String path,
												 @RequestHeader HttpHeaders headers, @RequestParam boolean binary){
		...
    }
}
`````

### Pattern MVC

J'utilise le design pattern MVC avec la structure Controller - Service - Objet - Repository (voir architecture plus
haut)

### Algorithme pour télécharger un dossier

Voici mon algorithme qui récupère un à un les fichiers d'un dossier sur le serveur FTP pour les mettre dans un zip.

`````java
public class FTPService{
	public Resource downloadDirectory(int serverId, String token, String path, String username, String password) {
		User user = Utils.findObjectOrThrow(() -> userRepository.findByToken(token), log);
		Server server = Utils.findObjectOrThrow(() -> serverRepository.findByIdAndUser(serverId, user), log);
		List<FTPFile> files = ftpConnector.list(server, path, username, password);
		File zipFile = getTemporaryFile("ftpZip.zip");
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		for (FTPFile file : files) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ftpConnector.downloadFile(server, file.getName(), username, password, bos, FileType.BINARY);
			ZipEntry zipEntry = new ZipEntry(file.getName());
			zipOut.putNextEntry(zipEntry);
			zipOut.write(bos.toByteArray());
			bos.close();
		}
		zipOut.close();
		fos.close();
		return new FileSystemResource(zipFile);
	}
}
`````

### Notes

Pas d'utilisation d'interfaces pour les services, car pas utile à ce niveau de l'application. En revanche, utilisée avec
FTPConnector car on peut avoir tendance à changer la façon dont on se connecte au serveur FTP.
