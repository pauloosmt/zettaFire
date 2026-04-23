import { View, Text, Pressable } from 'react-native';
import { useRouter } from 'expo-router';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

export default function ForgotPasswordScreen() {
    const router = useRouter();

    return (
        <ScreenWrapper className="justify-center">
            <View className="mb-10">
                <Text className="text-3xl font-bold text-text mb-2">Recuperar Senha</Text>
                <Text className="text-gray-500">
                    Digite seu e-mail e enviaremos um código de recuperação.
                </Text>
            </View>

            <Input label="E-mail" placeholder="seu@email.com" keyboardType="email-address" />

            <Button
                title="Enviar Código"
                variant="orange"
                onPress={() => alert('E-mail enviado!')}
            />

            <Pressable onPress={() => router.back()} className="mt-8 items-center">
                <Text className="text-fire-orange font-semibold">Voltar para o Login</Text>
            </Pressable>
        </ScreenWrapper>
    );
}