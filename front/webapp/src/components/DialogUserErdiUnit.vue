<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-dialog v-model="show" lazy persistent width="45%">
        <v-card class="record-card pa-3">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    {{ action === 'create' ? 'Создать запись' : 'Редактировать запись'}}
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text>
                <v-layout row class="pa-0 mb-2">
                    <v-flex align-self-center md6>
                        Тип записи
                    </v-flex>
                    <v-flex align-self-center md6>
                        <v-autocomplete class="areaTextSize"
                                        v-model="unitInternal.type"
                                        :items="unitTypeList"
                                        color="primary"
                                        item="item.text"
                        ></v-autocomplete>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2">
                    <v-flex md4>
                        Значение
                    </v-flex>
                    <v-flex md8 class="value-font">
                        <v-textarea
                                v-model="unitInternal.value"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
            </v-card-text>
            <v-card-actions>
                <v-layout row>
                    <v-spacer></v-spacer>
                    <v-btn color="" flat @click="closeCard">Отмена</v-btn>
                    <v-btn color="primary" @click="save" :disabled="!isValid">Сохранить</v-btn>
                </v-layout>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    export default {
        name: "DialogUserErdiUnit",

        props: ['value', "action", "unit"],

        data() {
            return {
                unitInternal: {
                    type: null,
                    value: null
                },

                unitTypeList: [
                    {id: 0, text:'URL'},
                    {id: 1, text: 'DOMAIN'},
                    {id: 2, text: 'DOMAIN_MASK'},
                    {id: 3, text: 'IP_V4'},
                    {id: 4, text: 'IP_V6'},
                    {id: 5, text: 'IP_V4_SUBNET'},
                    {id: 6, text: 'IP_V6_SUBNET'},
                    ]
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        computed: {
            show: {
                set: function (v) {
                    this.$emit('input', v)
                },
                get: function () {
                    return this.value;
                }
            },

            isValid() {
                return !(this.unitInternal.type == null || this.unitInternal.value == null);
            }
        },

        watch: {
            unit(v) {
                if (v) {
                    console.log("yes " + v);
                    this.unitInternal = Object.assign({}, v)
                }
                else {
                    this.unitInternal = {
                        type: null,
                        value: null
                    }
                }
            }
        },

        mounted() {

        },

        methods: {
            closeCard() {
                this.show = false;
            },

            save() {
                console.log("save");
                this.$emit('update:unit', Object.assign({}, this.unitInternal));
                this.$emit('updateUnit');
                this.closeCard();
            }
        }
    }
</script>

<style>
    .value-font .v-input .v-textarea {
        display: none;
    }
    .value-font .v-input.v-textarea {
        padding-top: 0;
    }

    .value-font .v-input.v-input--checkbox {
        margin: 0;
        padding: 0;
    }
    .value-font .v-input.v-input--checkbox .v-messages{
        display: none;
    }

    .value-font .v-text-field__details {
        display: none;
    }
</style>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54)
    }
</style>