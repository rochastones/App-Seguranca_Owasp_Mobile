# App-Seguranca_Owasp_Mobile
Aplicativo de segurança mobile com aplicação das regras de segurança do Owasp top 10 para Mobile. 
SegurançaApp é uma aplicação Android que demonstra a implementação de mecanismos avançados de segurança mobile, incluindo ofuscação de código, anti-tampering, detecção de root e armazenamento criptografado.

Arquitetura
SegurançaApp/
├── MainActivity.java          # UI e controle de fluxo
├── SecurityHelper.java        # Criptografia + Anti-tampering
├── RootDetectionHelper.java   # Detecção de root
├── activity_main.xml          # Interface única
├── build.gradle.kts           # Configuração de build e ofuscação
└── proguard-rules.pro         # Regras de ofuscação

Fluxo de Operações
1. Usuário digita Nome e RA
2. Clica em "Salvar" → Dados criptografados com AES-256-GCM
3. Clica em "Recuperar" → Dados descriptografados e exibidos
4. Botões "Alterar"/"Deletar" habilitados apenas após recuperação
5. Status de segurança atualizado em tempo real

Resumo dos Requisitos Atendidos
Ofuscação de código (ProGuard/R8)
Proteção anti-tampering (Hash SHA-256)
Detecção de root (5 métodos)
EncryptedSharedPreferences (AES-256-GCM)
Interface única (uma tela)
Campos Nome e RA
Botão para armazenar dados
Botão para recuperar dados
