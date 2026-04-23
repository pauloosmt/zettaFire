import { View, Text, TextInput, TextInputProps } from 'react-native';

interface InputProps extends TextInputProps {
    label?: string;
    error?: string;
}

export function Input({ label, error, className = '', ...rest }: InputProps) {
    return (
        <View className={`w-full mb-4 ${className}`}>
            {label && (
                <Text className="text-text font-semibold mb-2 ml-1">
                    {label}
                </Text>
            )}

            <TextInput
                className={`w-full bg-white px-4 py-4 rounded-xl border ${error ? 'border-red-500' : 'border-gray-200'
                    } text-text`}
                placeholderTextColor="#9CA3AF"
                {...rest}
            />

            {error && (
                <Text className="text-red-500 text-sm mt-1 ml-1">
                    {error}
                </Text>
            )}
        </View>
    );
}