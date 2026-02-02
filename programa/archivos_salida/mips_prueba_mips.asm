.data
    .align 2
    newline: .asciiz "\n"
    str_0: .asciiz "Lenguaje funcionando"
    str_1: .asciiz "========================================
"
    str_2: .asciiz "      DEMOSTRACION DEL LENGUAJE
"
    str_3: .asciiz "========================================

"
    str_4: .asciiz "1) PRUEBA DE FLOATS
"
    str_5: .asciiz "   Valor de pi: "
    str_6: .asciiz "

"
    str_7: .asciiz "2) OPERADORES UNARIOS (++ y --)
"
    str_8: .asciiz "   Valor inicial: "
    str_9: .asciiz "
"
    str_10: .asciiz "   Despues de ++: "
    str_11: .asciiz "3) CONDICIONALES (DECIDE OF)
"
    str_12: .asciiz "   A = "
    str_13: .asciiz "   B = "
    str_14: .asciiz "   -> A es MAYOR que B
"
    str_15: .asciiz "   -> A es IGUAL a B
"
    str_16: .asciiz "   -> A es MENOR que B
"
    str_17: .asciiz "4) BUCLE FOR
"
    str_18: .asciiz "   Conteo: "
    str_19: .asciiz " "
    str_20: .asciiz "5) MATRIZ GLOBAL (2x2)
"
    str_21: .asciiz "   Fila "
    str_22: .asciiz ": "
    str_23: .asciiz "["
    str_24: .asciiz "] "
    str_25: .asciiz "6) FACTORIAL CON ENTRADA
"
    str_26: .asciiz "   Ingrese un numero: "
    str_27: .asciiz "   factorial("
    str_28: .asciiz ") = "
    str_29: .asciiz "7) ANALISIS DEL FACTORIAL
"
    str_30: .asciiz "   -> Muy pequeño
"
    str_31: .asciiz "   -> Tamaño pequeño
"
    str_32: .asciiz "   -> Tamaño mediano
"
    str_33: .asciiz "   -> Tamaño grande
"
    str_34: .asciiz "   -> EXTREMADAMENTE GRANDE
"
    str_35: .asciiz "
========================================
"
    str_36: .asciiz "           FIN DEL PROGRAMA
"
    entero: .word 0
    decimal: .float 0.0
    estado: .word 0
    letra: .word 0
    texto: .word 0
    matrizGlobal: .word 0, 0, 0, 0

    float_const_37: .float 3.14159
.text
    .globl main
main:
    # --- Inicialización Global ---
    li $t0, 0
    sw $t0, 0($sp)
    li $t0, 42
    sw $t0, entero
    l.s $f0, float_const_37
    mfc1 $t0, $f0
    sw $t0, decimal
    li $t0, 1
    sw $t0, estado
    li $t0, 65
    sw $t0, letra
    la $t0, str_0
    sw $t0, texto
    li $t0, 0
    li $t1, 0
    li $t5, 1
    li $t2, 2
    mul $t3, $t0, $t2
    add $t3, $t3, $t1
    sll $t3, $t3, 2
    la $t4, matrizGlobal
    add $t4, $t4, $t3
    sw $t5, 0($t4)
    li $t0, 0
    li $t1, 1
    li $t5, 2
    li $t2, 2
    mul $t3, $t0, $t2
    add $t3, $t3, $t1
    sll $t3, $t3, 2
    la $t4, matrizGlobal
    add $t4, $t4, $t3
    sw $t5, 0($t4)
    li $t0, 1
    li $t1, 0
    li $t5, 3
    li $t2, 2
    mul $t3, $t0, $t2
    add $t3, $t3, $t1
    sll $t3, $t3, 2
    la $t4, matrizGlobal
    add $t4, $t4, $t3
    sw $t5, 0($t4)
    li $t0, 1
    li $t1, 1
    li $t5, 4
    li $t2, 2
    mul $t3, $t0, $t2
    add $t3, $t3, $t1
    sll $t3, $t3, 2
    la $t4, matrizGlobal
    add $t4, $t4, $t3
    sw $t5, 0($t4)
    li $t0, 0
    sw $t0, 0($sp)    # Actualizar memoria
    # -----------------------------
    jal NAVIDAD
    li $v0, 10
    syscall

sumar:
    # Prólogo de sumar
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro a
    sw $a1, 4($sp)    # Guardar parámetro b

    lw $t0, 0($sp)    # Cargar local/temp: a
    lw $t1, 4($sp)    # Cargar local/temp: b
    addu $t2, $t0, $t1
    sw $t2, 8($sp)
    lw $v0, 8($sp)    # Cargar local/temp: t0
    # Epílogo de función
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra
    # Epílogo por defecto
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra

factorial:
    # Prólogo de factorial
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro n

    lw $t0, 0($sp)    # Cargar local/temp: n
    li $t1, 1
    sle $t2, $t0, $t1
    sw $t2, 4($sp)
    lw $t0, 4($sp)    # Cargar local/temp: t1
    bnez $t0, L1
    j L2
L1:
    li $v0, 1
    # Epílogo de función
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra
    j L0
L2:
    lw $t0, 0($sp)    # Cargar local/temp: n
    li $t1, 1
    subu $t2, $t0, $t1
    sw $t2, 8($sp)
    lw $a0, 8($sp)    # Cargar local/temp: t2
    jal factorial
    sw $v0, 12($sp)
    lw $t0, 0($sp)    # Cargar local/temp: n
    lw $t1, 12($sp)    # Cargar local/temp: t3
    mul $t2, $t0, $t1
    sw $t2, 16($sp)
    lw $v0, 16($sp)    # Cargar local/temp: t4
    # Epílogo de función
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra
L0:
    # Epílogo por defecto
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra

calculos:
    # Prólogo de calculos
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro x
    sw $a1, 4($sp)    # Guardar parámetro y

    li $t0, 0
    sw $t0, 8($sp)
    li $t0, 0
    sw $t0, 8($sp)    # Actualizar memoria
    lw $t0, 0($sp)    # Cargar local/temp: x
    lw $t1, 4($sp)    # Cargar local/temp: y
    addu $t2, $t0, $t1
    sw $t2, 12($sp)
    lw $t0, 12($sp)    # Cargar local/temp: t5
    sw $t0, 8($sp)    # Actualizar memoria
    lw $t0, 0($sp)    # Cargar local/temp: x
    lw $t1, 4($sp)    # Cargar local/temp: y
    subu $t2, $t0, $t1
    sw $t2, 16($sp)
    lw $t0, 16($sp)    # Cargar local/temp: t6
    sw $t0, 8($sp)    # Actualizar memoria
    lw $t0, 0($sp)    # Cargar local/temp: x
    lw $t1, 4($sp)    # Cargar local/temp: y
    mul $t2, $t0, $t1
    sw $t2, 20($sp)
    lw $t0, 20($sp)    # Cargar local/temp: t7
    sw $t0, 8($sp)    # Actualizar memoria
    lw $t0, 0($sp)    # Cargar local/temp: x
    lw $t1, 4($sp)    # Cargar local/temp: y
    sw $t2, 24($sp)
    lw $t0, 24($sp)    # Cargar local/temp: t8
    sw $t0, 8($sp)    # Actualizar memoria
    lw $t0, 0($sp)    # Cargar local/temp: x
    lw $t1, 4($sp)    # Cargar local/temp: y
    div $t0, $t1
    mflo $t2
    sw $t2, 28($sp)
    lw $t0, 28($sp)    # Cargar local/temp: t9
    sw $t0, 8($sp)    # Actualizar memoria
    lw $t0, 0($sp)    # Cargar local/temp: x
    lw $t1, 4($sp)    # Cargar local/temp: y
    div $t0, $t1
    mfhi $t2
    sw $t2, 32($sp)
    lw $t0, 32($sp)    # Cargar local/temp: t10
    sw $t0, 8($sp)    # Actualizar memoria
    lw $t0, 0($sp)    # Cargar local/temp: x
    sw $t0, 8($sp)    # Actualizar memoria
    lw $v0, 8($sp)    # Cargar local/temp: temp
    # Epílogo de función
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra
    # Epílogo por defecto
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra

NAVIDAD:
    # Prólogo de NAVIDAD
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256

    la $a0, str_1
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_2
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_3
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_4
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_5
    li $v0, 4          # Imprimir String
    syscall
    l.s $f12, decimal     # Cargar float global
    li $v0, 2          # Syscall 2 = Float
    syscall
    la $a0, str_6
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_7
    li $v0, 4          # Imprimir String
    syscall
    li $t0, 0
    sw $t0, 0($sp)
    li $t0, 10
    sw $t0, 0($sp)    # Actualizar memoria
    la $a0, str_8
    li $v0, 4          # Imprimir String
    syscall
    lw $a0, 0($sp)    # Cargar local/temp: num
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_9
    li $v0, 4          # Imprimir String
    syscall
    lw $t0, 0($sp)    # Cargar local/temp: num
    li $t1, 1
    addu $t2, $t0, $t1
    sw $t2, 4($sp)
    lw $t0, 4($sp)    # Cargar local/temp: t11
    sw $t0, 0($sp)    # Actualizar memoria
    la $a0, str_10
    li $v0, 4          # Imprimir String
    syscall
    lw $a0, 0($sp)    # Cargar local/temp: num
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_9
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_11
    li $v0, 4          # Imprimir String
    syscall
    li $t0, 0
    sw $t0, 8($sp)
    li $t0, 50
    sw $t0, 8($sp)    # Actualizar memoria
    li $t0, 0
    sw $t0, 12($sp)
    li $t0, 100
    sw $t0, 12($sp)    # Actualizar memoria
    la $a0, str_12
    li $v0, 4          # Imprimir String
    syscall
    lw $a0, 8($sp)    # Cargar local/temp: A
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_13
    li $v0, 4          # Imprimir String
    syscall
    lw $a0, 12($sp)    # Cargar local/temp: B
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_9
    li $v0, 4          # Imprimir String
    syscall
    lw $t0, 8($sp)    # Cargar local/temp: A
    lw $t1, 12($sp)    # Cargar local/temp: B
    sgt $t2, $t0, $t1
    sw $t2, 16($sp)
    lw $t0, 16($sp)    # Cargar local/temp: t12
    bnez $t0, L4
    j L5
L4:
    la $a0, str_14
    li $v0, 4          # Imprimir String
    syscall
    j L3
L5:
    lw $t0, 8($sp)    # Cargar local/temp: A
    lw $t1, 12($sp)    # Cargar local/temp: B
    seq $t2, $t0, $t1
    sw $t2, 20($sp)
    lw $t0, 20($sp)    # Cargar local/temp: t13
    bnez $t0, L6
    j L7
L6:
    la $a0, str_15
    li $v0, 4          # Imprimir String
    syscall
    j L3
L7:
    la $a0, str_16
    li $v0, 4          # Imprimir String
    syscall
L3:
    la $a0, str_9
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_17
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_18
    li $v0, 4          # Imprimir String
    syscall
    li $t0, 0
    sw $t0, 24($sp)
L8:
    lw $t0, 24($sp)    # Cargar local/temp: i
    li $t1, 5
    sle $t2, $t0, $t1
    sw $t2, 28($sp)
    lw $t0, 28($sp)    # Cargar local/temp: t14
    bnez $t0, L9
    j L10
L9:
    lw $a0, 24($sp)    # Cargar local/temp: i
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_19
    li $v0, 4          # Imprimir String
    syscall
    lw $t0, 24($sp)    # Cargar local/temp: i
    li $t1, 1
    addu $t2, $t0, $t1
    sw $t2, 32($sp)
    lw $t0, 32($sp)    # Cargar local/temp: t15
    sw $t0, 24($sp)    # Actualizar memoria
    j L8
L10:
    la $a0, str_6
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_20
    li $v0, 4          # Imprimir String
    syscall
    li $t0, 0
    sw $t0, 36($sp)
L11:
    lw $t0, 36($sp)    # Cargar local/temp: f
    li $t1, 2
    slt $t2, $t0, $t1
    sw $t2, 40($sp)
    lw $t0, 40($sp)    # Cargar local/temp: t16
    bnez $t0, L12
    j L13
L12:
    la $a0, str_21
    li $v0, 4          # Imprimir String
    syscall
    lw $a0, 36($sp)    # Cargar local/temp: f
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_22
    li $v0, 4          # Imprimir String
    syscall
    li $t0, 0
    sw $t0, 44($sp)
L14:
    lw $t0, 44($sp)    # Cargar local/temp: c
    li $t1, 2
    slt $t2, $t0, $t1
    sw $t2, 48($sp)
    lw $t0, 48($sp)    # Cargar local/temp: t17
    bnez $t0, L15
    j L16
L15:
    la $a0, str_23
    li $v0, 4          # Imprimir String
    syscall
    lw $t0, 36($sp)    # Cargar local/temp: f
    lw $t1, 44($sp)    # Cargar local/temp: c
    li $t2, 2
    mul $t3, $t0, $t2
    add $t3, $t3, $t1
    sll $t3, $t3, 2
    la $t4, matrizGlobal
    add $t4, $t4, $t3
    lw $t5, 0($t4)
    sw $t5, 52($sp)
    lw $a0, 52($sp)    # Cargar local/temp: t18
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_24
    li $v0, 4          # Imprimir String
    syscall
    lw $t0, 44($sp)    # Cargar local/temp: c
    li $t1, 1
    addu $t2, $t0, $t1
    sw $t2, 56($sp)
    lw $t0, 56($sp)    # Cargar local/temp: t19
    sw $t0, 44($sp)    # Actualizar memoria
    j L14
L16:
    la $a0, str_9
    li $v0, 4          # Imprimir String
    syscall
    lw $t0, 36($sp)    # Cargar local/temp: f
    li $t1, 1
    addu $t2, $t0, $t1
    sw $t2, 60($sp)
    lw $t0, 60($sp)    # Cargar local/temp: t20
    sw $t0, 36($sp)    # Actualizar memoria
    j L11
L13:
    la $a0, str_9
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_25
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_26
    li $v0, 4          # Imprimir String
    syscall
    li $t0, 0
    sw $t0, 64($sp)
    li $t0, 0
    sw $t0, 64($sp)    # Actualizar memoria
    li $v0, 5          # Leer entero
    syscall
    sw $v0, 64($sp)    # Actualizar memoria
    li $t0, 0
    sw $t0, 68($sp)
    lw $a0, 64($sp)    # Cargar local/temp: numUsuario
    jal factorial
    sw $v0, 72($sp)
    lw $t0, 72($sp)    # Cargar local/temp: t21
    sw $t0, 68($sp)    # Actualizar memoria
    la $a0, str_27
    li $v0, 4          # Imprimir String
    syscall
    lw $a0, 64($sp)    # Cargar local/temp: numUsuario
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_28
    li $v0, 4          # Imprimir String
    syscall
    lw $a0, 68($sp)    # Cargar local/temp: resultadoFact
    li $v0, 1          # Syscall 1 = Int
    syscall
    la $a0, str_6
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_29
    li $v0, 4          # Imprimir String
    syscall
    lw $t0, 68($sp)    # Cargar local/temp: resultadoFact
    li $t1, 10
    slt $t2, $t0, $t1
    sw $t2, 76($sp)
    lw $t0, 76($sp)    # Cargar local/temp: t22
    bnez $t0, L18
    j L19
L18:
    la $a0, str_30
    li $v0, 4          # Imprimir String
    syscall
    j L17
L19:
    lw $t0, 68($sp)    # Cargar local/temp: resultadoFact
    li $t1, 10
    sge $t2, $t0, $t1
    sw $t2, 80($sp)
    lw $t0, 68($sp)    # Cargar local/temp: resultadoFact
    li $t1, 100
    slt $t2, $t0, $t1
    sw $t2, 84($sp)
    lw $t0, 80($sp)    # Cargar local/temp: t23
    lw $t1, 84($sp)    # Cargar local/temp: t24
    and $t2, $t0, $t1
    sw $t2, 88($sp)
    lw $t0, 88($sp)    # Cargar local/temp: t25
    bnez $t0, L20
    j L21
L20:
    la $a0, str_31
    li $v0, 4          # Imprimir String
    syscall
    j L17
L21:
    lw $t0, 68($sp)    # Cargar local/temp: resultadoFact
    li $t1, 100
    sge $t2, $t0, $t1
    sw $t2, 92($sp)
    lw $t0, 68($sp)    # Cargar local/temp: resultadoFact
    li $t1, 1000
    slt $t2, $t0, $t1
    sw $t2, 96($sp)
    lw $t0, 92($sp)    # Cargar local/temp: t26
    lw $t1, 96($sp)    # Cargar local/temp: t27
    and $t2, $t0, $t1
    sw $t2, 100($sp)
    lw $t0, 100($sp)    # Cargar local/temp: t28
    bnez $t0, L22
    j L23
L22:
    la $a0, str_32
    li $v0, 4          # Imprimir String
    syscall
    j L17
L23:
    lw $t0, 68($sp)    # Cargar local/temp: resultadoFact
    li $t1, 1000
    sge $t2, $t0, $t1
    sw $t2, 104($sp)
    lw $t0, 68($sp)    # Cargar local/temp: resultadoFact
    li $t1, 10000
    slt $t2, $t0, $t1
    sw $t2, 108($sp)
    lw $t0, 104($sp)    # Cargar local/temp: t29
    lw $t1, 108($sp)    # Cargar local/temp: t30
    and $t2, $t0, $t1
    sw $t2, 112($sp)
    lw $t0, 112($sp)    # Cargar local/temp: t31
    bnez $t0, L24
    j L25
L24:
    la $a0, str_33
    li $v0, 4          # Imprimir String
    syscall
    j L17
L25:
    la $a0, str_34
    li $v0, 4          # Imprimir String
    syscall
L17:
    la $a0, str_35
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_36
    li $v0, 4          # Imprimir String
    syscall
    la $a0, str_1
    li $v0, 4          # Imprimir String
    syscall
    li $v0, 0
    # Epílogo de función
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    li $v0, 10
    syscall
    # Epílogo por defecto
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra

