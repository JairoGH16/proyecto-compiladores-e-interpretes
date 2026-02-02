.data
    .align 2
    newline: .asciiz "\n"
    str_0: .asciiz "Hola Mundo Global"
    str_1: .asciiz "Ejecutando bucles"
    str_2: .asciiz "Ingrese un valor para el contador:"
    str_3: .asciiz "Contador en rango medio"
    str_4: .asciiz "Contador alto o flag activo"
    str_5: .asciiz "Contador bajo"
    str_6: .asciiz "Iteracion loop:"
    str_7: .asciiz "Factorial de 5 es:"
    str_8: .asciiz "Resultado math:"
    variableGlobal: .word 0
    pi: .float 0.0
    estadoGlobal: .word 0
    letraGlobal: .word 0
    saludoGlobal: .word 0
    matrizGlobal: .word 0, 0, 0, 0

    float_const_9: .float 50.5
    float_const_10: .float 10.5
    float_const_11: .float 2.0
.text
    .globl main
main:
    jal NAVIDAD
    li $v0, 10
    syscall

factorial:
    # Prólogo de factorial
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro n

    lw $t0, 0($sp)    # Leer n de la pila
    li $t1, 1
    sle $t2, $t0, $t1
    sw $t2, 4($sp)    # Nuevo espacio para t0
    lw $t0, 4($sp)    # Leer t0 de la pila
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
    lw $t0, 0($sp)    # Leer n de la pila
    li $t1, 1
    subu $t2, $t0, $t1
    sw $t2, 8($sp)    # Nuevo espacio para t1
    lw $a0, 8($sp)    # Leer t1 de la pila
    jal factorial
    sw $v0, 12($sp)    # Nuevo espacio para t2
    lw $t0, 0($sp)    # Leer n de la pila
    lw $t1, 12($sp)    # Leer t2 de la pila
    mul $t2, $t0, $t1
    sw $t2, 16($sp)    # Nuevo espacio para t3
    lw $v0, 16($sp)    # Leer t3 de la pila
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

operacionesMatematicas:
    # Prólogo de operacionesMatematicas
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro a
    sw $a1, 4($sp)    # Guardar parámetro b

    li $t0, 0
    sw $t0, 8($sp)    # Nuevo espacio para resultado
    lw $t0, 0($sp)    # Leer a de la pila
    lw $t1, 4($sp)    # Leer b de la pila
    addu $t2, $t0, $t1
    sw $t2, 12($sp)    # Nuevo espacio para t4
    lw $t0, 0($sp)    # Leer a de la pila
    lw $t1, 4($sp)    # Leer b de la pila
    subu $t2, $t0, $t1
    sw $t2, 16($sp)    # Nuevo espacio para t5
    lw $t0, 12($sp)    # Leer t4 de la pila
    lw $t1, 16($sp)    # Leer t5 de la pila
    mul $t2, $t0, $t1
    sw $t2, 20($sp)    # Nuevo espacio para t6
    lw $t0, 20($sp)    # Leer t6 de la pila
    lw $t1, 0($sp)    # Leer a de la pila
    sw $t2, 24($sp)    # Nuevo espacio para t7
    lw $t0, 24($sp)    # Leer t7 de la pila
    sw $t0, 8($sp)    # Guardar en resultado
    lw $t0, 0($sp)    # Leer a de la pila
    lw $t1, 4($sp)    # Leer b de la pila
    div $t0, $t1
    mflo $t2
    sw $t2, 28($sp)    # Nuevo espacio para t8
    lw $t0, 0($sp)    # Leer a de la pila
    lw $t1, 4($sp)    # Leer b de la pila
    div $t0, $t1
    mfhi $t2
    sw $t2, 32($sp)    # Nuevo espacio para t9
    lw $t0, 28($sp)    # Leer t8 de la pila
    lw $t1, 32($sp)    # Leer t9 de la pila
    addu $t2, $t0, $t1
    sw $t2, 36($sp)    # Nuevo espacio para t10
    lw $t0, 36($sp)    # Leer t10 de la pila
    sw $t0, 8($sp)    # Guardar en resultado
    lw $v0, 8($sp)    # Leer resultado de la pila
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

    li $t0, 0
    sw $t0, 0($sp)    # Nuevo espacio para contador
    l.s $f0, float_const_9
    mfc1 $t0, $f0    # Mover bits de float a registro entero
    sw $t0, 4($sp)    # Nuevo espacio para limite
    la $t0, str_1    # Cargar dirección del string
    sw $t0, 8($sp)    # Nuevo espacio para mensaje
    li $t0, 0
    sw $t0, 12($sp)    # Nuevo espacio para flag
    # DECLARE notas[3][3]
    # Reservar 36 bytes en pila
    # Asignar a array local notas[0][0] = 10
    li $t0, 10
    sw $t0, 16($sp)
    # Asignar a array local notas[0][1] = 20
    li $t0, 20
    sw $t0, 20($sp)
    # Asignar a array local notas[0][2] = 30
    li $t0, 30
    sw $t0, 24($sp)
    # Asignar a array local notas[1][0] = 40
    li $t0, 40
    sw $t0, 28($sp)
    # Asignar a array local notas[1][1] = 50
    li $t0, 50
    sw $t0, 32($sp)
    # Asignar a array local notas[1][2] = 60
    li $t0, 60
    sw $t0, 36($sp)
    # Asignar a array local notas[2][0] = 70
    li $t0, 70
    sw $t0, 40($sp)
    # Asignar a array local notas[2][1] = 80
    li $t0, 80
    sw $t0, 44($sp)
    # Asignar a array local notas[2][2] = 90
    li $t0, 90
    sw $t0, 48($sp)
    la $a0, str_2    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    li $v0, 5          # Leer entero
    syscall
    sw $v0, 0($sp)    # Guardar en contador
    lw $a0, 0($sp)    # Leer contador de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $a0, variableGlobal    # Leer global variableGlobal
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $t0, 0($sp)    # Leer contador de la pila
    li $t1, 10
    sgt $t2, $t0, $t1
    sw $t2, 52($sp)    # Nuevo espacio para t11
    lw $t0, 0($sp)    # Leer contador de la pila
    li $t1, 100
    slt $t2, $t0, $t1
    sw $t2, 56($sp)    # Nuevo espacio para t12
    lw $t0, 52($sp)    # Leer t11 de la pila
    lw $t1, 56($sp)    # Leer t12 de la pila
    and $t2, $t0, $t1
    sw $t2, 60($sp)    # Nuevo espacio para t13
    lw $t0, 60($sp)    # Leer t13 de la pila
    bnez $t0, L4
    j L5
L4:
    la $a0, str_3    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    j L3
L5:
    lw $t0, 0($sp)    # Leer contador de la pila
    li $t1, 100
    sge $t2, $t0, $t1
    sw $t2, 64($sp)    # Nuevo espacio para t14
    lw $t0, 12($sp)    # Leer flag de la pila
    li $t1, 1
    seq $t2, $t0, $t1
    sw $t2, 68($sp)    # Nuevo espacio para t15
    lw $t0, 64($sp)    # Leer t14 de la pila
    lw $t1, 68($sp)    # Leer t15 de la pila
    or $t2, $t0, $t1
    sw $t2, 72($sp)    # Nuevo espacio para t16
    lw $t0, 72($sp)    # Leer t16 de la pila
    bnez $t0, L6
    j L7
L6:
    la $a0, str_4    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    j L3
L7:
    la $a0, str_5    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
L3:
    li $t0, -5
    sw $t0, 76($sp)    # Nuevo espacio para negativo
    lw $t0, 0($sp)    # Leer contador de la pila
    li $t1, 1
    addu $t2, $t0, $t1
    sw $t2, 80($sp)    # Nuevo espacio para t17
    lw $t0, 80($sp)    # Leer t17 de la pila
    sw $t0, 0($sp)    # Guardar en contador
    lw $t0, 0($sp)    # Leer contador de la pila
    sw $t0, 84($sp)    # Nuevo espacio para preIncremento
L8:
    la $a0, str_6    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $a0, 0($sp)    # Leer contador de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $t0, 0($sp)    # Leer contador de la pila
    li $t1, 5
    addu $t2, $t0, $t1
    sw $t2, 88($sp)    # Nuevo espacio para t18
    lw $t0, 88($sp)    # Leer t18 de la pila
    sw $t0, 0($sp)    # Guardar en contador
    lw $t0, 0($sp)    # Leer contador de la pila
    li $t1, 20
    sgt $t2, $t0, $t1
    sw $t2, 92($sp)    # Nuevo espacio para t19
    lw $t0, 92($sp)    # Leer t19 de la pila
    bnez $t0, L9
    j L8
L9:
    li $t0, 0
    sw $t0, 96($sp)    # Nuevo espacio para i
L10:
    lw $t0, 96($sp)    # Leer i de la pila
    li $t1, 3
    slt $t2, $t0, $t1
    sw $t2, 100($sp)    # Nuevo espacio para t20
    lw $t0, 100($sp)    # Leer t20 de la pila
    bnez $t0, L11
    j L12
L11:
    li $t0, 0
    sw $t0, 104($sp)    # Nuevo espacio para j
L13:
    lw $t0, 104($sp)    # Leer j de la pila
    li $t1, 3
    slt $t2, $t0, $t1
    sw $t2, 108($sp)    # Nuevo espacio para t21
    lw $t0, 108($sp)    # Leer t21 de la pila
    bnez $t0, L14
    j L15
L14:
    lw $t0, 96($sp)    # Leer i de la pila
    lw $t1, 104($sp)    # Leer j de la pila
    # Acceso a array local notas[i][j]
    li $t2, 3    # Número de columnas
    mul $t3, $t0, $t2       # i * numCols
    add $t3, $t3, $t1       # i * numCols + j
    sll $t3, $t3, 2         # Multiplicar por 4
    addi $t3, $t3, 16  # Agregar offset base
    add $t4, $sp, $t3       # Dirección = $sp + offset
    lw $t5, 0($t4)          # Cargar elemento
    sw $t5, 112($sp)    # Nuevo espacio para t22
    lw $t0, 112($sp)    # Leer t22 de la pila
    lw $t1, 0($sp)    # Leer contador de la pila
    addu $t2, $t0, $t1
    sw $t2, 116($sp)    # Nuevo espacio para t23
    lw $t0, 96($sp)    # Leer i de la pila
    lw $t1, 104($sp)    # Leer j de la pila
    # Asignar a array local notas[i][j] = t23
    li $t2, 3    # Número de columnas
    mul $t3, $t0, $t2       # i * numCols
    add $t3, $t3, $t1       # i * numCols + j
    sll $t3, $t3, 2         # Multiplicar por 4
    addi $t3, $t3, 16  # Agregar offset base
    add $t4, $sp, $t3       # Dirección = $sp + offset
    lw $t5, 116($sp)    # Leer t23 de la pila
    sw $t5, 0($t4)          # Guardar elemento
    lw $t0, 96($sp)    # Leer i de la pila
    lw $t1, 104($sp)    # Leer j de la pila
    # Acceso a array local notas[i][j]
    li $t2, 3    # Número de columnas
    mul $t3, $t0, $t2       # i * numCols
    add $t3, $t3, $t1       # i * numCols + j
    sll $t3, $t3, 2         # Multiplicar por 4
    addi $t3, $t3, 16  # Agregar offset base
    add $t4, $sp, $t3       # Dirección = $sp + offset
    lw $t5, 0($t4)          # Cargar elemento
    sw $t5, 120($sp)    # Nuevo espacio para t24
    lw $a0, 120($sp)    # Leer t24 de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    # Incremento de control para loop
    lw $t0, 104($sp)
    addiu $t0, $t0, 1
    sw $t0, 104($sp)
    j L13
L15:
    # Incremento de control para loop
    lw $t0, 96($sp)
    addiu $t0, $t0, 1
    sw $t0, 96($sp)
    j L10
L12:
    lw $t0, 0($sp)    # Leer contador de la pila
    li $t1, 0
    seq $t2, $t0, $t1
    sw $t2, 124($sp)    # Nuevo espacio para t25
    lw $t0, 128($sp)    # Leer ! t25 de la pila
    sw $t0, 132($sp)    # Nuevo espacio para t26
    lw $t0, letraGlobal    # Leer global letraGlobal
    li $t1, 88    # 'X'
    sne $t2, $t0, $t1
    sw $t2, 136($sp)    # Nuevo espacio para t27
    lw $t0, 132($sp)    # Leer t26 de la pila
    lw $t1, 136($sp)    # Leer t27 de la pila
    and $t2, $t0, $t1
    sw $t2, 140($sp)    # Nuevo espacio para t28
    lw $t0, 140($sp)    # Leer t28 de la pila
    sw $t0, 144($sp)    # Nuevo espacio para validacion
    lw $t0, 144($sp)    # Leer validacion de la pila
    bnez $t0, L17
    j L18
L17:
    lw $a0, 8($sp)    # Leer mensaje de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    j L16
L18:
L16:
    li $a0, 5
    jal factorial
    sw $v0, 148($sp)    # Nuevo espacio para t29
    lw $t0, 148($sp)    # Leer t29 de la pila
    sw $t0, 152($sp)    # Nuevo espacio para rFact
    l.s $f0, float_const_10
    mfc1 $a0, $f0    # Mover bits de float a registro entero
    l.s $f0, float_const_11
    mfc1 $a1, $f0    # Mover bits de float a registro entero
    jal operacionesMatematicas
    sw $v0, 156($sp)    # Nuevo espacio para t30
    lw $t0, 156($sp)    # Leer t30 de la pila
    sw $t0, 160($sp)    # Nuevo espacio para rMat
    la $a0, str_7    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $a0, 152($sp)    # Leer rFact de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    la $a0, str_8    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $a0, 160($sp)    # Leer rMat de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
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

