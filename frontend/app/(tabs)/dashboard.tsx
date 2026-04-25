import { View, Text, FlatList, Pressable, Alert } from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';

type UserRole = 'ADMIN' | 'USER';

interface User {
    idUser: string;
    name: string;
    email: string;
    phone: string;
    createdAt: string;
    userRole: UserRole;
}

const MOCK_STATS = {
    totalFires: 342,
    totalAlerts: 1854,
};

const MOCK_USERS: User[] = [
    {
        idUser: '550e8400-e29b-41d4-a716-446655440000',
        name: 'Henrique Assis',
        email: 'henrique@zetta.com.br',
        phone: '(35) 99999-9999',
        createdAt: '2026-04-20',
        userRole: 'ADMIN',
    },
    {
        idUser: '6ba7b810-9dad-11d1-80b4-00c04fd430c8',
        name: 'Irys Silva',
        email: 'irys@email.com',
        phone: '(31) 98888-8888',
        createdAt: '2026-04-21',
        userRole: 'USER',
    },
    {
        idUser: '7c9e6679-7425-40de-944b-e07fc1f90ae7',
        name: 'João Pedro',
        email: 'joao@email.com',
        phone: '(11) 97777-7777',
        createdAt: '2026-04-22',
        userRole: 'USER',
    }
];

export default function DashboardScreen() {
    const handleDeleteUser = (id: string, name: string) => {
        Alert.alert(
            "Excluir Usuário",
            `Tem certeza que deseja excluir ${name}?`,
            [
                { text: "Cancelar", style: "cancel" },
                { text: "Excluir", style: "destructive", onPress: () => console.log('Excluindo usuário:', id) }
            ]
        );
    };

    const renderUser = ({ item }: { item: User }) => {
        const shortId = item.idUser.split('-')[0];

        return (
            <View className="bg-white p-4 rounded-xl mb-3 border border-gray-200 flex-row justify-between items-center shadow-sm">
                <View className="flex-1">
                    <Text className="font-bold text-lg text-text">{item.name}</Text>
                    <Text className="text-sm text-gray-500">ID: {shortId}...</Text>
                    <View className="mt-1 self-start px-2 py-0.5 bg-gray-100 rounded-md">
                        <Text className="text-xs text-gray-600 font-semibold">{item.userRole}</Text>
                    </View>
                </View>

                <Pressable 
                    className="bg-red-100 px-4 py-2 rounded-lg ml-2"
                    onPress={() => handleDeleteUser(item.idUser, item.name)}
                >
                    <Text className="text-red-700 font-bold">Excluir</Text>
                </Pressable>
            </View>
        );
    };

    return (
        <ScreenWrapper>
            <View className="px-5 pt-6 pb-4">
                <Text className="text-3xl font-bold text-text mb-6">Painel Administrativo</Text>
                
                <View className="flex-row justify-between mb-8 gap-4">
                    <View className="flex-1 bg-white p-4 rounded-2xl border border-gray-200 items-center shadow-sm">
                        <Text className="text-gray-500 text-sm font-semibold mb-1 text-center">Incêndios Registrados</Text>
                        <Text className="text-3xl font-bold text-fire-orange">{MOCK_STATS.totalFires}</Text>
                    </View>

                    <View className="flex-1 bg-white p-4 rounded-2xl border border-gray-200 items-center shadow-sm">
                        <Text className="text-gray-500 text-sm font-semibold mb-1 text-center">Alertas Enviados</Text>
                        <Text className="text-3xl font-bold text-green-600">{MOCK_STATS.totalAlerts}</Text>
                    </View>
                </View>

                <Text className="text-xl font-bold text-text mb-4">Gerenciamento de Usuários</Text>
            </View>

            <FlatList
                data={MOCK_USERS}
                keyExtractor={(item) => item.idUser}
                renderItem={renderUser}
                contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 40 }}
                showsVerticalScrollIndicator={false}
            />
        </ScreenWrapper>
    );
}