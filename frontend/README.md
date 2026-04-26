# Zetta Fire - Frontend

Aplicativo móvel desenvolvido em **React Native** com **Expo** para monitoramento de focos de incêndio no estado de Minas Gerais.

## 🔧 Tecnologias

| Tecnologia | Descrição |
|------------|-----------|
| **Expo** | Framework para criação de apps React Native com ferramentas integradas |
| **React Native** | Biblioteca para desenvolvimento de apps nativos iOS e Android |
| **Expo Router** | Sistema de navegação baseado em arquivos para Expo |
| **NativeWind** | Utilitário de estilização usando classes Tailwind CSS |
| **TypeScript** | Superset JavaScript com tipagem estática |
| **Expo Notifications** | Sistema de notificações push via Firebase Cloud Messaging |

## 📱 Funcionalidades

- **Autenticação**: Login e registro de usuários
- **Dashboard**: Visualização de estatísticas e alertas
- **Mapa Interativo**: Exibição de focos de incêndio em tempo real
- **Notificações Push**: Alertas sobre novos focos detectados
- **Interface Responsiva**: Design adaptável para diferentes tamanhos de tela

## 🚀 Começando

### Pré-requisitos

- Node.js 18+
- npm ou yarn
- Conta Expo (para notificações push)
- Projeto Firebase configurado

### Instalação

```bash
# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npx expo start

# Executar no Android
npx expo run:android

# Executar no iOS
npx expo run:ios
```

### Build para Produção

```bash
# Gerar build nativo local
npx expo prebuild

# Build via EAS (cloud)
npx eas-cli build --platform android --profile production
```

## 📁 Estrutura de Diretórios

```
frontend/
├── app/                    # Páginas e rotas (Expo Router)
│   ├── (auth)/            # Rotas de autenticação
│   │   ├── login.tsx
│   │   └── register.tsx
│   ├── (tabs)/            # Rotas com tabs navigation
│   │   ├── dashboard.tsx
│   │   ├── map.tsx
│   │   └── index.tsx
│   ├── _layout.tsx        # Layout raiz
│   └── index.tsx          # Entry point
├── assets/                # Imagens, fontes, sons
├── components/            # Componentes reutilizáveis
│   ├── layout/
│   └── ui/
├── utils/                 # Funções utilitárias
├── services/              # Configuração do Axios
├── theme/                 # Configurações de tema
└── package.json
```

## 🔔 Notificações Push

O app utiliza **Firebase Cloud Messaging (FCM)** para enviar notificações push. Para configurar:

1. Criar projeto no [Firebase Console](https://console.firebase.google.com/)
2. Adicionar app Android com package `com.henrique117.zettafire`
3. Baixar `google-services.json` e colocar em `android/app/`
4. Configurar credenciais no Expo