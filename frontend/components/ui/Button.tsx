import { Pressable, Text, ActivityIndicator } from 'react-native';

interface ButtonProps {
    title: string;
    onPress: () => void;
    variant?: 'orange' | 'yellow' | 'green' | 'outline';
    isLoading?: boolean;
    className?: string;
}

export function Button({
    title,
    onPress,
    variant = 'orange',
    isLoading = false,
    className = ''
}: ButtonProps) {

    const variantClasses = {
        orange: "bg-fire-orange",
        yellow: "bg-fire-yellow",
        green: "bg-fire-green",
        outline: "bg-transparent border-2 border-fire-orange",
    };

    const textClasses = {
        orange: "text-text",
        yellow: "text-text",
        green: "text-text",
        outline: "text-fire-orange",
    };

    return (
        <Pressable
            onPress={onPress}
            disabled={isLoading}
            className={`py-4 rounded-xl items-center justify-center active:opacity-80 ${variantClasses[variant]} ${isLoading ? 'opacity-70' : ''} ${className}`}
        >
            {isLoading ? (
                <ActivityIndicator color="#111827" />
            ) : (
                <Text className={`${textClasses[variant]} font-bold text-lg`}>
                    {title}
                </Text>
            )}
        </Pressable>
    );
}