package ru.andrewb.charm.back.mapper;

public interface Mapper<From, To> {

    To map(From obj);
}
