.data
    .align 2
    newline: .asciiz "\n"
    str_0: .asciiz "Hola  endl ~&|! endl & ?  mundo"
    str_1: .asciiz "entra al if"
    str_2: .asciiz "!|"
    _s2_: .word 0
    ss: .word 0
    ff: .float 0.0
    ii: .word 0
    _var_2: .word 0
    _var2_: .word 0

    float_const_3: .float 1.1
    float_const_4: .float -10.5
    float_const_5: .float -1.9
    float_const_6: .float 1.1
    float_const_7: .float -5.6
    float_const_8: .float -0.01
    float_const_9: .float -0.01
    float_const_10: .float -6.7
    float_const_11: .float 45.6
    float_const_12: .float 76.3
    float_const_13: .float 3.7
    float_const_14: .float 6.7
    float_const_15: .float 8.9
.text
    .globl main
main:
    jal NAVIDAD
    li $v0, 10
    syscall

_func2_:
    # Prólogo de _func2_
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro _b1_
    sw $a1, 4($sp)    # Guardar parámetro _i1_

    li $t0, 0
    sw $t0, 8($sp)    # Nuevo espacio para i
    li $v0, 1
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

_mi_:
    # Prólogo de _mi_
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro _dif_
    sw $a1, 4($sp)    # Guardar parámetro _otra_

    li $t0, 0
    sw $t0, 8($sp)    # Nuevo espacio para _otra21_
    la $t0, str_0    # Cargar dirección del string
    sw $t0, 12($sp)    # Nuevo espacio para _str_
L0:
    lw $t0, 16($sp)    # Leer i de la pila
    li $t1, 10
    sge $t2, $t0, $t1
    sw $t2, 20($sp)    # Nuevo espacio para t0
    lw $t0, 20($sp)    # Leer t0 de la pila
    bnez $t0, L1
    j L2
L1:
L3:
    lw $t0, _var_2    # Leer global _var_2
    li $t1, 1
    div $t0, $t1
    mflo $t2
    sw $t2, 24($sp)    # Nuevo espacio para t1
    lw $t0, 24($sp)    # Leer t1 de la pila
    sw $t0, _var_2    # Guardar en global _var_2
    lw $t0, 28($sp)    # Leer _var_ de la pila
    li $t1, 1
    subu $t2, $t0, $t1
    sw $t2, 32($sp)    # Nuevo espacio para t2
    lw $t0, 32($sp)    # Leer t2 de la pila
    sw $t0, 28($sp)    # Guardar en _var_
    lw $t0, _var2_    # Leer global _var2_
    li $t1, 12
    sne $t2, $t0, $t1
    sw $t2, 36($sp)    # Nuevo espacio para t3
    li $t0, 34
    li $t1, 35
    mul $t2, $t0, $t1
    sw $t2, 40($sp)    # Nuevo espacio para t4
    li $t0, 12
    lw $t1, 40($sp)    # Leer t4 de la pila
    seq $t2, $t0, $t1
    sw $t2, 44($sp)    # Nuevo espacio para t5
    lw $t0, 48($sp)    # Leer ! t5 de la pila
    sw $t0, 52($sp)    # Nuevo espacio para t6
    lw $t0, 36($sp)    # Leer t3 de la pila
    lw $t1, 52($sp)    # Leer t6 de la pila
    and $t2, $t0, $t1
    sw $t2, 56($sp)    # Nuevo espacio para t7
    lw $t0, 56($sp)    # Leer t7 de la pila
    bnez $t0, L4
    j L3
L4:
    li $t0, 0
    sw $t0, 60($sp)    # Nuevo espacio para var
    li $t0, 24
    lw $t1, 60($sp)    # Leer var de la pila
    sge $t2, $t0, $t1
    sw $t2, 64($sp)    # Nuevo espacio para t8
    lw $t0, 64($sp)    # Leer t8 de la pila
    bnez $t0, L6
    j L7
L6:
    la $a0, str_1    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    j L5
L7:
L5:
    li $t0, 1
    bnez $t0, L9
    # Incremento de control para loop
    lw $t0, 16($sp)
    addiu $t0, $t0, 1
    sw $t0, 16($sp)
    j L10
L9:
    lw $a0, 28($sp)    # Leer _var_ de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    j L8
L10:
    lw $t0, 4($sp)    # Leer _otra_ de la pila
    l.s $f0, float_const_3
    mfc1 $t1, $f0    # Mover bits de float a registro entero
    seq $t2, $t0, $t1
    sw $t2, 68($sp)    # Nuevo espacio para t9
    lw $t0, 68($sp)    # Leer t9 de la pila
    bnez $t0, L12
    j L13
L12:
    la $a0, str_2    # Cargar dirección del string
    li $v0, 4          # Imprimir String
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    j L11
L13:
    lw $t0, 72($sp)    # Leer _otrax_ de la pila
    li $t1, 2
    seq $t2, $t0, $t1
    sw $t2, 76($sp)    # Nuevo espacio para t10
    lw $t0, 76($sp)    # Leer t10 de la pila
    bnez $t0, L14
    j L15
L14:
    li $t0, 10
    sw $t0, 72($sp)    # Guardar en _otrax_
    j L11
L15:
    l.s $f0, float_const_4
    mfc1 $t0, $f0    # Mover bits de float a registro entero
    l.s $f0, float_const_5
    mfc1 $t1, $f0    # Mover bits de float a registro entero
    mul $t2, $t0, $t1
    sw $t2, 80($sp)    # Nuevo espacio para t11
    lw $t0, 80($sp)    # Leer t11 de la pila
    sw $t0, 4($sp)    # Guardar en _otra_
L11:
L8:
    j L0
L2:
    l.s $f0, float_const_6
    mfc1 $v0, $f0    # Mover bits de float a registro entero
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

_miOtraFun_:
    # Prólogo de _miOtraFun_
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256

    l.s $f0, float_const_7
    mfc1 $v0, $f0    # Mover bits de float a registro entero
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

    l.s $f0, float_const_8
    mfc1 $t0, $f0    # Mover bits de float a registro entero
    sw $t0, 0($sp)    # Nuevo espacio para i
    li $t0, 33    # '!'
    sw $t0, 4($sp)    # Nuevo espacio para _miChar_
    li $t0, 33    # '!'
    sw $t0, 8($sp)    # Nuevo espacio para _miChar2_
    # DECLARE _arr_[3][3]
    # Reservar 36 bytes en pila
    # Asignar a array local _arr_[0][0] = 1
    li $t0, 1
    sw $t0, 12($sp)
    # Asignar a array local _arr_[0][1] = 5
    li $t0, 5
    sw $t0, 16($sp)
    # Asignar a array local _arr_[0][2] = 10
    li $t0, 10
    sw $t0, 20($sp)
    # Asignar a array local _arr_[1][0] = 2
    li $t0, 2
    sw $t0, 24($sp)
    # Asignar a array local _arr_[1][1] = 4
    li $t0, 4
    sw $t0, 28($sp)
    # Asignar a array local _arr_[1][2] = 5
    li $t0, 5
    sw $t0, 32($sp)
    # Asignar a array local _arr_[2][0] = 20
    li $t0, 20
    sw $t0, 36($sp)
    # Asignar a array local _arr_[2][1] = 40
    li $t0, 40
    sw $t0, 40($sp)
    # Asignar a array local _arr_[2][2] = 50
    li $t0, 50
    sw $t0, 44($sp)
    lw $t0, 48($sp)    # Leer _i_ de la pila
    li $t1, 1
    addu $t2, $t0, $t1
    sw $t2, 52($sp)    # Nuevo espacio para t12
    lw $t0, 52($sp)    # Leer t12 de la pila
    sw $t0, 48($sp)    # Guardar en _i_
    li $t0, 67
    lw $t1, 48($sp)    # Leer _i_ de la pila
    addu $t2, $t0, $t1
    sw $t2, 56($sp)    # Nuevo espacio para t13
    lw $t0, 56($sp)    # Leer t13 de la pila
    li $t1, 23
    # Asignar a array local _arr_[i][j] = -0.01
    li $t2, 3    # Número de columnas
    mul $t3, $t0, $t2       # i * numCols
    add $t3, $t3, $t1       # i * numCols + j
    sll $t3, $t3, 2         # Multiplicar por 4
    addi $t3, $t3, 12  # Agregar offset base
    add $t4, $sp, $t3       # Dirección = $sp + offset
    l.s $f0, float_const_9
    mfc1 $t5, $f0    # Mover bits de float a registro entero
    sw $t5, 0($t4)          # Guardar elemento
    li $t0, 0
    sw $t0, 60($sp)    # Nuevo espacio para _b1_
    li $t0, 0
    sw $t0, 64($sp)    # Nuevo espacio para _s1_
    li $v0, 5          # Leer entero
    syscall
    sw $v0, 64($sp)    # Guardar en _s1_
    lw $a0, 60($sp)    # Leer _b1_ de la pila
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    li $a0, 1
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    l.s $f0, float_const_10
    mfc1 $a0, $f0    # Mover bits de float a registro entero
    li $v0, 1          # Imprimir Entero/Char
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    li $t0, 56
    sw $t0, 68($sp)    # Nuevo espacio para fl1
    lw $t0, 68($sp)    # Leer fl1 de la pila
    li $t1, 1
    subu $t2, $t0, $t1
    sw $t2, 72($sp)    # Nuevo espacio para t14
    lw $t0, 72($sp)    # Leer t14 de la pila
    sw $t0, 68($sp)    # Guardar en fl1
    lw $t0, 76($sp)    # Leer in1 de la pila
    li $t1, 1
    addu $t2, $t0, $t1
    sw $t2, 80($sp)    # Nuevo espacio para t15
    lw $t0, 80($sp)    # Leer t15 de la pila
    sw $t0, 76($sp)    # Guardar en in1
    li $t0, -14
    lw $t1, 76($sp)    # Leer in1 de la pila
    div $t0, $t1
    mfhi $t2
    sw $t2, 84($sp)    # Nuevo espacio para t16
    lw $t0, 68($sp)    # Leer fl1 de la pila
    lw $t1, 84($sp)    # Leer t16 de la pila
    subu $t2, $t0, $t1
    sw $t2, 88($sp)    # Nuevo espacio para t17
    li $t0, 7
    li $t1, 15
    sw $t2, 92($sp)    # Nuevo espacio para t18
    lw $t0, 88($sp)    # Leer t17 de la pila
    lw $t1, 92($sp)    # Leer t18 de la pila
    addu $t2, $t0, $t1
    sw $t2, 96($sp)    # Nuevo espacio para t19
    lw $t0, 96($sp)    # Leer t19 de la pila
    sw $t0, 76($sp)    # Guardar en in1
    l.s $f0, float_const_11
    mfc1 $t0, $f0    # Mover bits de float a registro entero
    l.s $f0, float_const_12
    mfc1 $t1, $f0    # Mover bits de float a registro entero
    div $t0, $t1
    mfhi $t2
    sw $t2, 100($sp)    # Nuevo espacio para t20
    l.s $f0, float_const_13
    mfc1 $t0, $f0    # Mover bits de float a registro entero
    lw $t1, 100($sp)    # Leer t20 de la pila
    addu $t2, $t0, $t1
    sw $t2, 104($sp)    # Nuevo espacio para t21
    lw $t0, 104($sp)    # Leer t21 de la pila
    sw $t0, 108($sp)    # Nuevo espacio para fl2
    li $t0, 67
    li $t1, 23
    # Acceso a array local _arr_[67][23]
    li $t2, 3    # Número de columnas
    mul $t3, $t0, $t2       # i * numCols
    add $t3, $t3, $t1       # i * numCols + j
    sll $t3, $t3, 2         # Multiplicar por 4
    addi $t3, $t3, 12  # Agregar offset base
    add $t4, $sp, $t3       # Dirección = $sp + offset
    lw $t5, 0($t4)          # Cargar elemento
    sw $t5, 112($sp)    # Nuevo espacio para t22
    li $t0, 10
    lw $t1, 112($sp)    # Leer t22 de la pila
    subu $t2, $t0, $t1
    sw $t2, 116($sp)    # Nuevo espacio para t23
    # Asignar a array local _arr_[2][2] = t23
    lw $t0, 116($sp)    # Leer t23 de la pila
    sw $t0, 44($sp)
    li $t0, 4
    li $t1, 4
    div $t0, $t1
    mfhi $t2
    sw $t2, 120($sp)    # Nuevo espacio para t24
    lw $t0, 120($sp)    # Leer t24 de la pila
    sw $t0, 68($sp)    # Guardar en fl1
    l.s $f0, float_const_14
    mfc1 $t0, $f0    # Mover bits de float a registro entero
    l.s $f0, float_const_15
    mfc1 $t1, $f0    # Mover bits de float a registro entero
    sne $t2, $t0, $t1
    sw $t2, 124($sp)    # Nuevo espacio para t25
    lw $t0, 124($sp)    # Leer t25 de la pila
    sw $t0, 128($sp)    # Nuevo espacio para bl0
    li $t0, 1
    li $t1, 0
    sne $t2, $t0, $t1
    sw $t2, 132($sp)    # Nuevo espacio para t26
    lw $t0, 132($sp)    # Leer t26 de la pila
    sw $t0, 128($sp)    # Guardar en bl0
    lw $t0, 76($sp)    # Leer in1 de la pila
    lw $t1, 68($sp)    # Leer fl1 de la pila
    sge $t2, $t0, $t1
    sw $t2, 136($sp)    # Nuevo espacio para t27
    li $a0, 1
    lw $a1, 76($sp)    # Leer in1 de la pila
    jal _func2_
    sw $v0, 140($sp)    # Nuevo espacio para t28
    lw $t0, 140($sp)    # Leer t28 de la pila
    li $t1, 56
    sgt $t2, $t0, $t1
    sw $t2, 144($sp)    # Nuevo espacio para t29
    lw $t0, 148($sp)    # Leer ! t29 de la pila
    sw $t0, 152($sp)    # Nuevo espacio para t30
    li $t0, 0
    lw $t1, 152($sp)    # Leer t30 de la pila
    and $t2, $t0, $t1
    sw $t2, 156($sp)    # Nuevo espacio para t31
    lw $t0, 136($sp)    # Leer t27 de la pila
    lw $t1, 156($sp)    # Leer t31 de la pila
    or $t2, $t0, $t1
    sw $t2, 160($sp)    # Nuevo espacio para t32
    lw $t0, 160($sp)    # Leer t32 de la pila
    sw $t0, 164($sp)    # Nuevo espacio para bl1
    li $a0, 1
    lw $a1, 76($sp)    # Leer in1 de la pila
    jal _func2_
    sw $v0, 168($sp)    # Nuevo espacio para t33
    li $t0, 1
    lw $t1, 168($sp)    # Leer t33 de la pila
    addu $t2, $t0, $t1
    sw $t2, 172($sp)    # Nuevo espacio para t34
    lw $v0, 172($sp)    # Leer t34 de la pila
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

