import React, { useState, useEffect } from 'react';
import { View, Text, Pressable, Image, ScrollView, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api from '../../services/api';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { fetchPushToken } from '../../utils/fetchToken';

export default function RegisterScreen() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [token, setToken] = useState<string | null>(null);

    const [form, setForm] = useState({
        name: '', email: '', password: '', phone: '',
        street: '', cep: '', city: '', number: '', state: '', district: ''
    });

    useEffect(() => {
        async function getToken() {
            const pushToken = await fetchPushToken();
            setToken(pushToken);
        }
        getToken();
    }, []);

    const maskPhone = (value: string) => {
        let r = value.replace(/\D/g, "");
        r = r.substring(0, 11);
        if (r.length > 10) r = r.replace(/^(\d\d)(\d{5})(\d{4}).*/, "($1)$2-$3");
        else if (r.length > 5) r = r.replace(/^(\d\d)(\d{4})(\d{0,4}).*/, "($1)$2-$3");
        else if (r.length > 2) r = r.replace(/^(\d\d)(\d{0,5})/, "($1)$2");
        else if (r.length > 0) r = r.replace(/^(\d*)/, "($1");
        return r;
    };

    const handleFetchCep = async (cep: string) => {
        const cleanCep = cep.replace(/\D/g, '');
        if (cleanCep.length === 8) {
            try {
                const response = await api.get(`https://viacep.com.br/ws/${cleanCep}/json/`);
                if (response.data.erro) return Alert.alert("Erro", "CEP não encontrado.");
                setForm(prev => ({
                    ...prev,
                    street: response.data.logradouro,
                    district: response.data.bairro,
                    city: response.data.localidade,
                    state: response.data.uf
                }));
            } catch (error) {
                Alert.alert("Erro", "Falha ao buscar CEP.");
            }
        }
    };

    const handleRegister = async () => {
        if (!form.name || !form.email || !form.password || !form.phone) {
            return Alert.alert("Atenção", "Preencha todos os campos obrigatórios.");
        }

        setLoading(true);

        const payload = {
            name: form.name,
            email: form.email,
            password: form.password,
            phone: form.phone,
            pushToken: token || "",
            address: {
                street: form.street, cep: form.cep, city: form.city,
                number: form.number, state: form.state, district: form.district
            }
        };

        try {
            const response = await api.post('/auth/register', payload);
            if (response.data.token) {
                await AsyncStorage.setItem('@zettafire:token', response.data.token);
            }
            Alert.alert("Sucesso", "Conta criada com sucesso!");
            router.replace('/(tabs)/');
        } catch (error: any) {
            Alert.alert("Erro no Cadastro", error.response?.data?.message || "Erro desconhecido");
        } finally {
            setLoading(false);
        }
    };

    return (
        <ScreenWrapper>
            <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 40 }}>
                <View className="items-center mt-6 mb-8 w-full">
                    <Image source={require('../../assets/images/logo_square.png')} className="w-full h-32" resizeMode="contain" />
                    <Text className="text-3xl font-bold text-text mt-4">Criar Conta</Text>
                </View>

                <View className="gap-y-4">
                    <Text className="text-fire-orange font-bold text-lg border-b border-gray-200">Dados Pessoais</Text>
                    <Input label="Nome Completo" placeholder="Ex: João Silva" value={form.name} onChangeText={(t) => setForm(prev => ({ ...prev, name: t }))} />
                    <Input label="E-mail" placeholder="seu@email.com" keyboardType="email-address" autoCapitalize="none" value={form.email} onChangeText={(t) => setForm(prev => ({ ...prev, email: t }))} />
                    <Input label="Telefone" placeholder="(35) 99999-9999" keyboardType="phone-pad" value={form.phone} onChangeText={(t) => setForm(prev => ({ ...prev, phone: maskPhone(t) }))} />
                    <Input label="Senha" placeholder="Mínimo 6 caracteres" secureTextEntry value={form.password} onChangeText={(t) => setForm(prev => ({ ...prev, password: t }))} />

                    <Text className="text-fire-orange font-bold text-lg border-b border-gray-200 mt-4">Endereço</Text>
                    <Input label="CEP" placeholder="Somente números" keyboardType="numeric" maxLength={8} value={form.cep} onChangeText={(t) => {
                        const clean = t.replace(/\D/g, "").substring(0, 8);
                        setForm(prev => ({ ...prev, cep: clean }));
                        if (clean.length === 8) handleFetchCep(clean);
                    }} />
                    <View className="flex-row gap-x-2">
                        <View className="flex-1"><Input label="Rua" placeholder="Logradouro" value={form.street} onChangeText={(t) => setForm(prev => ({ ...prev, street: t }))} /></View>
                        <View className="w-24"><Input label="Nº" placeholder="123" keyboardType="numeric" value={form.number} onChangeText={(t) => setForm(prev => ({ ...prev, number: t }))} /></View>
                    </View>
                    <Input label="Bairro" placeholder="Ex: Centro" value={form.district} onChangeText={(t) => setForm(prev => ({ ...prev, district: t }))} />
                    <Input label="Cidade" placeholder="Sua cidade" value={form.city} editable={false} />
                </View>

                <Button title={loading ? "Enviando..." : "Finalizar Cadastro"} variant="green" className="mt-8" onPress={handleRegister} isLoading={loading} />

                <Pressable onPress={() => router.back()} className="mt-6 items-center">
                    <Text className="text-gray-500">Já tem conta? <Text className="text-fire-orange font-semibold">Faça Login</Text></Text>
                </Pressable>
            </ScrollView>
        </ScreenWrapper>
    );
}