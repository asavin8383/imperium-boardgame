n = int(input("Количество чисел в последовательности: "))
cipher_count = 0
for _ in range(n):
    new_number = int(input("Введите число: "))
    while new_number:
        if new_number % 10 > 5:
            cipher_count += 1
        new_number //= 10
else:
    print(cipher_count)

