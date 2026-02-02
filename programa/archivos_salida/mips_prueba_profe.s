.data
    .align 2
    newline: .asciiz "\n"
    _s2_: .word 0
    ss: .word 0
    ff: .float 0.0
    ii: .word 0
    _var_2: .word 0
    _var2_: .word 0

.text
    .align 2
    .globl main

main:

    jal NAVIDAD
    li $v0, 10
    syscall

_func2_:
    # Prólogo
    addi $sp, $sp, -128    # Reservar espacio en pila
    sw $ra, 124($sp)       # Guardar dirección de retorno
    sw $fp, 120($sp)       # Guardar frame pointer
    addi $fp, $sp, 128     # Establecer nuevo frame pointer
    sw $a0, 0($sp)    # Guardar parámetro _b1_
    sw $a1, 4($sp)    # Guardar parámetro _i1_

    li $t0, 0
    sw $t0, 8($sp)
    li $v0, 1
    # Return
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra
    # Epílogo por defecto
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra

_mi_:
    # Prólogo
    addi $sp, $sp, -128    # Reservar espacio en pila
    sw $ra, 124($sp)       # Guardar dirección de retorno
    sw $fp, 120($sp)       # Guardar frame pointer
    addi $fp, $sp, 128     # Establecer nuevo frame pointer
    sw $a0, 0($sp)    # Guardar parámetro _dif_
    sw $a1, 4($sp)    # Guardar parámetro _otra_

    li $t0, 0
    sw $t0, 8($sp)
L0:
    lw $t0, 12($sp)
    li $t1, 10
    sge $t2, $t0, $t1    # i >= 10
    sw $t2, 16($sp)
    lw $t0, 16($sp)
    bnez $t0, L1    # Saltar si t0 != 0
    j L2
L1:
L3:
    lw $t0, _var_2
    li $t1, 1
    # Operador desconocido: //
    lw $t0, 20($sp)
    sw $t0, _var_2
    li $t1, 1
    sub $t2, $t0, $t1    # _var_ - 1
    sw $t2, 24($sp)
    lw $t0, 24($sp)
    sw $t0, 28($sp)
    lw $t0, _var2_
    li $t1, 12
    sne $t2, $t0, $t1    # _var2_ != 12
    sw $t2, 32($sp)
    li $t0, 34
    li $t1, 35
    mul $t2, $t0, $t1    # 34 * 35
    sw $t2, 36($sp)
    li $t0, 12
    lw $t1, 36($sp)
    seq $t2, $t0, $t1    # 12 == t4
    sw $t2, 40($sp)
    lw $t0, 40($sp)
    seq $t2, $t0, $zero    # !t5
    sw $t2, 44($sp)
    lw $t0, 32($sp)
    lw $t1, 44($sp)
    and $t2, $t0, $t1    # t3 && t6
    sw $t2, 48($sp)
    lw $t0, 48($sp)
    bnez $t0, L4    # Saltar si t7 != 0
    j L3
L4:
    li $t0, 0
    sw $t0, 52($sp)
    li $t0, 24
    lw $t1, 52($sp)
    sge $t2, $t0, $t1    # 24 >= var
    sw $t2, 56($sp)
    lw $t0, 56($sp)
    bnez $t0, L6    # Saltar si t8 != 0
    j L7
L6:
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline    # Imprimir salto de línea
    li $v0, 4          # Syscall print_string
    syscall
    j L5
L7:
L5:
    li $t0, 1
    bnez $t0, L9    # Saltar si 1 != 0
    j L10
L9:
    lw $a0, 28($sp)
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline    # Imprimir salto de línea
    li $v0, 4          # Syscall print_string
    syscall
    j L8
L10:
    lw $t0, 4($sp)
    li $t1, 1.1
    seq $t2, $t0, $t1    # _otra_ == 1.1
    sw $t2, 60($sp)
    lw $t0, 60($sp)
    bnez $t0, L12    # Saltar si t9 != 0
    j L13
L12:
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline    # Imprimir salto de línea
    li $v0, 4          # Syscall print_string
    syscall
    j L11
L13:
    li $t1, 2
    seq $t2, $t0, $t1    # _otrax_ == 2
    sw $t2, 64($sp)
    lw $t0, 64($sp)
    bnez $t0, L14    # Saltar si t10 != 0
    j L15
L14:
    li $t0, 10
    sw $t0, 68($sp)
    j L11
L15:
    li $t0, -10.5
    li $t1, -1.9
    mul $t2, $t0, $t1    # -10.5 * -1.9
    sw $t2, 72($sp)
    lw $t0, 72($sp)
    sw $t0, 4($sp)
L11:
L8:
    j L0
L2:
    li $v0, 1.1
    # Return
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra
    # Epílogo por defecto
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra

_miOtraFun_:
    # Prólogo
    addi $sp, $sp, -128    # Reservar espacio en pila
    sw $ra, 124($sp)       # Guardar dirección de retorno
    sw $fp, 120($sp)       # Guardar frame pointer
    addi $fp, $sp, 128     # Establecer nuevo frame pointer

    li $v0, -5.6
    # Return
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra
    # Epílogo por defecto
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra

NAVIDAD:
    # Prólogo
    addi $sp, $sp, -128    # Reservar espacio en pila
    sw $ra, 124($sp)       # Guardar dirección de retorno
    sw $fp, 120($sp)       # Guardar frame pointer
    addi $fp, $sp, 128     # Establecer nuevo frame pointer

    li $t0, -0.01
    sw $t0, 0($sp)
    sw $t0, 4($sp)
    sw $t0, 8($sp)
    # DECLARE _arr_[3][3]
    # Reservar 36 bytes en pila
    li $t1, 1
    add $t2, $t0, $t1    # _i_ + 1
    sw $t2, 48($sp)
    lw $t0, 48($sp)
    sw $t0, 52($sp)
    li $t0, 67
    lw $t1, 52($sp)
    add $t2, $t0, $t1    # 67 + _i_
    sw $t2, 56($sp)
    li $t0, 0
    sw $t0, 60($sp)
    li $t0, 0
    sw $t0, 64($sp)
    lw $a0, 60($sp)
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline    # Imprimir salto de línea
    li $v0, 4          # Syscall print_string
    syscall
    li $a0, 1
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline    # Imprimir salto de línea
    li $v0, 4          # Syscall print_string
    syscall
    li $a0, -6.7
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline    # Imprimir salto de línea
    li $v0, 4          # Syscall print_string
    syscall
    li $t0, 56
    sw $t0, 68($sp)
    lw $t0, 68($sp)
    li $t1, 1
    sub $t2, $t0, $t1    # fl1 - 1
    sw $t2, 72($sp)
    lw $t0, 72($sp)
    sw $t0, 68($sp)
    lw $t0, 76($sp)
    li $t1, 1
    add $t2, $t0, $t1    # in1 + 1
    sw $t2, 80($sp)
    lw $t0, 80($sp)
    sw $t0, 76($sp)
    li $t0, -14
    lw $t1, 76($sp)
    div $t0, $t1         # -14 % in1
    mfhi $t2
    sw $t2, 84($sp)
    lw $t0, 68($sp)
    lw $t1, 84($sp)
    sub $t2, $t0, $t1    # fl1 - t16
    sw $t2, 88($sp)
    li $t0, 7
    li $t1, 15
    div $t0, $t1         # 7 / 15
    mflo $t2
    sw $t2, 92($sp)
    lw $t0, 88($sp)
    lw $t1, 92($sp)
    add $t2, $t0, $t1    # t17 + t18
    sw $t2, 96($sp)
    lw $t0, 96($sp)
    sw $t0, 76($sp)
    li $t0, 45.6
    li $t1, 76.3
    div $t0, $t1         # 45.6 % 76.3
    mfhi $t2
    sw $t2, 100($sp)
    li $t0, 3.7
    lw $t1, 100($sp)
    add $t2, $t0, $t1    # 3.7 + t20
    sw $t2, 104($sp)
    lw $t0, 104($sp)
    sw $t0, 108($sp)
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
    sw $t5, 112($sp)
    li $t0, 10
    lw $t1, 112($sp)
    sub $t2, $t0, $t1    # 10 - t22
    sw $t2, 116($sp)
    li $t0, 4
    li $t1, 4
    div $t0, $t1         # 4 % 4
    mfhi $t2
    sw $t2, 120($sp)
    lw $t0, 120($sp)
    sw $t0, 68($sp)
    li $t0, 6.7
    li $t1, 8.9
    sne $t2, $t0, $t1    # 6.7 != 8.9
    sw $t2, 124($sp)
    lw $t0, 124($sp)
    sw $t0, 128($sp)
    li $t0, 1
    li $t1, 0
    sne $t2, $t0, $t1    # 1 != 0
    sw $t2, 132($sp)
    lw $t0, 132($sp)
    sw $t0, 128($sp)
    lw $t0, 76($sp)
    lw $t1, 68($sp)
    sge $t2, $t0, $t1    # in1 >= fl1
    sw $t2, 136($sp)
    li $a0, 1
    lw $a1, 76($sp)
    jal _func2_    # Llamar _func2_
    sw $v0, 140($sp)
    lw $t0, 140($sp)
    li $t1, 56
    sgt $t2, $t0, $t1    # t28 > 56
    sw $t2, 144($sp)
    lw $t0, 144($sp)
    seq $t2, $t0, $zero    # !t29
    sw $t2, 148($sp)
    li $t0, 0
    lw $t1, 148($sp)
    and $t2, $t0, $t1    # 0 && t30
    sw $t2, 152($sp)
    lw $t0, 136($sp)
    lw $t1, 152($sp)
    or $t2, $t0, $t1     # t27 || t31
    sw $t2, 156($sp)
    lw $t0, 156($sp)
    sw $t0, 160($sp)
    li $a0, 1
    lw $a1, 76($sp)
    jal _func2_    # Llamar _func2_
    sw $v0, 164($sp)
    li $t0, 1
    lw $t1, 164($sp)
    add $t2, $t0, $t1    # 1 + t33
    sw $t2, 168($sp)
    lw $v0, 168($sp)
    # Return
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra
    # Epílogo por defecto
    lw $ra, 124($sp)
    lw $fp, 120($sp)
    addi $sp, $sp, 128
    jr $ra

