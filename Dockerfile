# Use an official Java image as the base image
FROM openjdk:17-jdk

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    git \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install IntelliJ IDEA Community Edition
RUN wget https://download.jetbrains.com/idea/ideaIC-2023.1.1.tar.gz -O /tmp/intellij.tar.gz \
    && tar -xzf /tmp/intellij.tar.gz -C /opt/ \
    && rm /tmp/intellij.tar.gz \
    && mv /opt/idea-IC-* /opt/intellij

# Set IntelliJ IDEA as the default command
CMD ["/opt/intellij/bin/idea.sh"]

# Install Kotlin command-line compiler
RUN wget https://github.com/JetBrains/kotlin/releases/download/v1.8.20/kotlin-compiler-1.8.20.zip -O /tmp/kotlin.zip \
    && unzip /tmp/kotlin.zip -d /opt/ \
    && rm /tmp/kotlin.zip

# Set Kotlin compiler in PATH
ENV PATH="/opt/kotlinc/bin:${PATH}"

# Create a workspace directory and copy the project files into it
WORKDIR /workspace
COPY . /workspace

# Compile and run the Kotlin script
CMD ["kotlinc", "BattleshipGame.kt", "-include-runtime", "-d", "BattleshipGame.jar"] 
CMD ["java", "-jar", "BattleshipGame.jar"]
