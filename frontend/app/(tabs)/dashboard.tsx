import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, Pressable, Alert, ActivityIndicator, RefreshControl } from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { useRouter } from 'expo-router';
import api from '../../services/api';

type UserRole = 'ADMIN' | 'USER';

interface User {
    idUser: string;
    name: string;
    email: string;
    phone: string;
    createdAt: string;
    userRole: UserRole;
}

export default function DashboardScreen() {
    const router = useRouter();
    const [users, setUsers] = useState<User[]>([]);
    const [totalFires, setTotalFires] = useState(0);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    const fetchData = async () => {
        try {
            const [usersResponse, firesResponse] = await Promise.all([
                api.get('/admin/all-users'),
                api.get('/fire-event/all') 
            ]);

            setUsers(Array.isArray(usersResponse.data) ? usersResponse.data : []);
            const firesData = firesResponse.data.content || firesResponse.data;
            setTotalFires(Array.isArray(firesData) ? firesData.length : 0);

        } catch (error: any) {
            if (error.response?.status === 403 || error.response?.status === 401) {
                Alert.alert("Acesso Negado", "Você não tem permissão de administrador.");
                router.replace('/(tabs)/');
            }
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => { fetchData(); }, []);

    const onRefresh = useCallback(() => {
        setRefreshing(true);
        fetchData();
    }, []);

    const handleDeleteUser = (email: string, name: string) => {
        Alert.alert(
            "Excluir Usuário",
            `Tem certeza que deseja excluir ${name}?`,
            [
                { text: "Cancelar", style: "cancel" },
                { 
                    text: "Excluir", 
                    style: "destructive", 
                    onPress: async () => {
                        try {
                            await api.delete(`/admin/delete`, { params: { email } });
                            fetchData(); 
                        } catch {
                            Alert.alert("Erro", "Não foi possível excluir o usuário.");
                        }
                    } 
                }
            ]
        );
    };

    const renderUser = ({ item }: { item: User }) => (
        <View className="bg-white p-4 rounded-xl mb-3 mx-5 border border-gray-200 flex-row justify-between items-center shadow-sm">
            <View className="flex-1">
                <Text className="font-bold text-lg text-text">{item.name}</Text>
                <Text className="text-sm text-gray-500">{item.email}</Text>
                <View className="mt-1 self-start px-2 py-0.5 bg-gray-100 rounded-md">
                    <Text className="text-xs text-gray-600 font-semibold">{item.userRole}</Text>
                </View>
            </View>
            {item.userRole !== 'ADMIN' && (
                <Pressable 
                    className="bg-red-100 px-4 py-2 rounded-lg active:opacity-70"
                    onPress={() => handleDeleteUser(item.email, item.name)}
                >
                    <Text className="text-red-700 font-bold">Excluir</Text>
                </Pressable>
            )}
        </View>
    );

    if (loading) return <ScreenWrapper><View className="flex-1 justify-center items-center"><ActivityIndicator size="large" color="#EA580C" /></View></ScreenWrapper>;

    return (
        <ScreenWrapper>
            <FlatList
                data={users}
                keyExtractor={(item, index) => item.idUser?.toString() || index.toString()}
                renderItem={renderUser}
                ListHeaderComponent={() => (
                    <View className="px-5 pt-6 pb-4">
                        <Text className="text-3xl font-bold text-text mb-6">Painel Administrativo</Text>
                        <View className="flex-row justify-between mb-8 gap-4">
                            <View className="flex-1 bg-white p-4 rounded-2xl border border-gray-200 items-center shadow-sm">
                                <Text className="text-gray-500 text-sm font-semibold mb-1 text-center">Incêndios</Text>
                                <Text className="text-3xl font-bold text-fire-orange">{totalFires}</Text>
                            </View>
                            <View className="flex-1 bg-white p-4 rounded-2xl border border-gray-200 items-center shadow-sm">
                                <Text className="text-gray-500 text-sm font-semibold mb-1 text-center">Usuários</Text>
                                <Text className="text-3xl font-bold text-green-600">{users.length}</Text>
                            </View>
                        </View>
                        <Text className="text-xl font-bold text-text mb-4">Gerenciamento de Usuários</Text>
                    </View>
                )}
                contentContainerStyle={{ paddingBottom: 40 }}
                refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={["#EA580C"]} />}
            />
        </ScreenWrapper>
    );
}